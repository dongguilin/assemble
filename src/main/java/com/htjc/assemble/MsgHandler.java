package com.htjc.assemble;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.google.common.collect.Lists;
import com.htjc.assemble.config.ProcedureConfig;
import com.htjc.assemble.model.Doc;
import com.htjc.assemble.pool.EsRestClientPool;
import com.htjc.assemble.procedure.AbstractProcedureChain;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

import static com.htjc.assemble.util.ConfigConstants.DEFAULT_PROCEDURES_NAME;
import static com.htjc.assemble.util.ConfigConstants._ES_ID;

/**
 * Created by guilin on 2016/9/7.
 */
public class MsgHandler implements MessageListenerOrderly {

    private static final Logger logger = LoggerFactory.getLogger(MsgHandler.class);

    private ThreadLocal<Map<String, AbstractProcedureChain>> chainMapThreadLocal = new ThreadLocal<>();

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        List<Doc> docList = null;
        try {
            //加工消息
            docList = processMessages(msgs);

            intoEs(docList);

        } catch (Throwable e) {
            logger.error("batch consumer {} docs error", docList.size(), e);
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
        return ConsumeOrderlyStatus.SUCCESS;
    }

    private void intoEs(List<Doc> docList) {
        //批量入库
        List<Response> responses = batchStoreIntoEsWithRestAPI(docList);

        //处理入库结果
        handleResponse(responses);
    }

    /**
     * 加工消息
     *
     * @param msgs
     * @return
     */
    private List<Doc> processMessages(List<MessageExt> msgs) {
        Map<String, AbstractProcedureChain> chainMap = chainMapThreadLocal.get();
        if (chainMap == null) {
            chainMap = ProcedureConfig.buildChainMap();
            chainMapThreadLocal.set(chainMap);
        }

        List<Doc> docList = new LinkedList<>();
        for (MessageExt messageExt : msgs) {
            Doc doc = new Doc();

            String body = new String(messageExt.getBody(), Charset.defaultCharset());
            Map<String, Object> dataMap = JSON.parseObject(body, Map.class);

            if (dataMap.containsKey(_ES_ID)) {
                doc.setId(dataMap.get(_ES_ID).toString());
            } else {
                doc.setId(messageExt.getMsgId() + messageExt.getBornTimestamp());
            }

            String type = messageExt.getTags();
            if (StringUtils.isBlank(type)) {
                if (dataMap.containsKey("tableName")) {
                    type = dataMap.get("tableName").toString();
                } else {
                    type = DEFAULT_PROCEDURES_NAME;
                }
            }
            doc.setType(type);

            doc.setBody(dataMap);

            AbstractProcedureChain chain = chainMap.get(type);
            if (chain == null) {
                chain = chainMap.get(DEFAULT_PROCEDURES_NAME);//默认处理
            }

            doc = chain.process(doc);

            if (doc != null) {
                docList.add(doc);
            }
        }
        return docList;
    }

    private List<Response> batchStoreIntoEsWithRestAPI(List<Doc> docList) {
        if (CollectionUtils.isEmpty(docList)) return null;
        List<RestClient> restClientList = null;
        List<Response> responses = new ArrayList<>();
        try {
            restClientList = EsRestClientPool.borrowObject(6000);
            StringBuffer buffer = new StringBuffer();
            for (Doc doc : docList) {
                buffer.append(buildIndexStr(doc));
            }
            HttpEntity entity = new NStringEntity(buffer.toString(), ContentType.APPLICATION_JSON);
            for (RestClient restClient : restClientList) {
                Response response = restClient.performRequest("POST", "/_bulk", Collections.<String, String>emptyMap(), entity);
                responses.add(response);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            EsRestClientPool.returnObject(restClientList);
        }
        return responses;
    }

    /**
     * 处理入库结果，入库失败的记录需保存
     *
     * @param responses
     */
    private void handleResponse(List<Response> responses) {
        if (CollectionUtils.isEmpty(responses)) return;
        try {
            for (Response response : responses) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String str = reader.readLine();
                IOUtils.closeQuietly(reader);

                JSONObject rootObj = JSON.parseObject(str);
                long took = rootObj.getLong("took");
                boolean hasFailures = rootObj.getBoolean("errors");
                JSONArray itemArray = rootObj.getJSONArray("items");
                if (hasFailures) {
                    List<JSONObject> errors = extractFailedRecs(itemArray);
                    String errorMsg = buildFailureMessage1(errors);
                    int succeed = itemArray.size() - errors.size();
                    logger.error("insert into elasticsearch {} docs success, {} docs fail, took {} millis, ({})",
                            succeed, errors.size(), took, response.getHost().toString());
                    throw new IllegalStateException(errorMsg);
                } else {
                    logger.info("insert into elasticsearch {} docs success, took {} millis ({})",
                            itemArray.size(), took, response.getHost().toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 抽取出入elasticsearch失败的记录
     *
     * @param array
     * @return
     */
    private List<JSONObject> extractFailedRecs(JSONArray array) {
        List<JSONObject> errors = Lists.newLinkedList();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = array.getJSONObject(i);
            JSONObject indexOrCreateObj = null;
            if (obj.containsKey("index")) {
                indexOrCreateObj = obj.getJSONObject("index");
            } else if (obj.containsKey("create")) {
                indexOrCreateObj = obj.getJSONObject("create");
            }

            int code = indexOrCreateObj.getInteger("status");
            if (code != HttpStatus.SC_OK) {
                errors.add(obj);
            }
        }
        return errors;
    }

    /**
     * 构建错误详情(最多前10条)
     *
     * @param list
     * @return
     */
    private String buildFailureMessage1(List<JSONObject> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("failure in bulk execution:\n");
        int size = list.size() > 10 ? 10 : list.size();
        for (int i = 0; i < size; i++) {
            sb.append(list.get(i).toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 构建索引文档语句
     *
     * @param doc
     * @return
     */
    private String buildIndexStr(Doc doc) {
        StringBuffer buffer = new StringBuffer();
        Map<String, Object> map = new HashMap<>();
        map.put("_index", doc.getIndex());
        map.put("_type", doc.getType());
        map.put("_id", doc.getId());
        buffer.append(JSON.toJSONString(Collections.singletonMap("index", map))).append("\n");
        buffer.append(JSON.toJSONString(doc.getBody())).append("\n");
        return buffer.toString();
    }

}
