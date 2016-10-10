package com.htjc.assemble;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerOrderly;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.htjc.assemble.config.ProcedureConfig;
import com.htjc.assemble.model.Doc;
import com.htjc.assemble.pool.EsClientPool;
import com.htjc.assemble.procedure.AbstractProcedureChain;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        try {
            //加工消息
            List<Doc> docList = processMessages(msgs);

            //批量入库
            BulkResponse response = batchStoreIntoEs(docList);

            //处理入库结果
            handleResponse(response);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
        return ConsumeOrderlyStatus.SUCCESS;
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

            String id = messageExt.getKeys();
            if (StringUtils.isBlank(id) && dataMap.containsKey(_ES_ID)) {
                id = dataMap.get(_ES_ID).toString();
            }
            doc.setId(id);

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

    /**
     * 批量入数据到elasticsearch
     *
     * @param docList
     */
    private BulkResponse batchStoreIntoEs(List<Doc> docList) {
        if (CollectionUtils.isEmpty(docList)) return null;

        Client esClient = null;
        BulkResponse response = null;
        try {
            esClient = EsClientPool.borrowObject(6000);
            BulkRequestBuilder builder = esClient.prepareBulk();
            for (Doc doc : docList) {

                IndexRequest request = null;
                if (doc.getId() != null) {
                    request = new IndexRequest(doc.getIndex(), doc.getType(), doc.getId());
                } else {
                    request = new IndexRequest(doc.getIndex(), doc.getType());
                }
                request.source(JSON.toJSONBytes(doc.getBody()));
                builder.add(request);
            }
            response = builder.execute().actionGet();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            EsClientPool.returnObject(esClient);
        }
        return response;
    }

    /**
     * 处理入库结果，入库失败的记录需保存
     *
     * @param response
     */
    private void handleResponse(BulkResponse response) {
        if (response == null) return;
        boolean hasFailures = response.hasFailures();
        if (!hasFailures) {
            logger.info("insert into elasticsearch {} docs success, take {} millis",
                    response.getItems().length, response.getTookInMillis());
        } else {
            List<BulkItemResponse.Failure> failureList = extractFailedRecs(response.getItems());
            int succeed = response.getItems().length - failureList.size();

            logger.error("insert into elasticsearch {} docs success, {} docs fail, take {} millis",
                    succeed, failureList.size(), response.getTookInMillis());
            String errorMsg = buildFailureMessage(failureList);
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * 构建错误详情(最多前10条)
     *
     * @param failureList
     * @return
     */
    public String buildFailureMessage(List<BulkItemResponse.Failure> failureList) {
        StringBuilder sb = new StringBuilder();
        sb.append("failure in bulk execution:");
        int size = failureList.size() > 10 ? 10 : failureList.size();

        for (int i = 0; i < size; i++) {
            BulkItemResponse.Failure failure = failureList.get(i);
            sb.append("\n[").append(i)
                    .append("]: index [").append(failure.getIndex()).append("], type [").append(failure.getType()).append("], id [").append(failure.getId())
                    .append("], message [").append(failure.getMessage()).append("]");
            try {
                throw failure.getCause();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return sb.toString();
    }


    /**
     * 抽取出入elasticsearch失败的记录
     *
     * @param responses
     * @return
     */
    private List<BulkItemResponse.Failure> extractFailedRecs(BulkItemResponse[] responses) {
        List<BulkItemResponse.Failure> failedList = new ArrayList();
        for (BulkItemResponse itemResponse : responses) {
            if (itemResponse.isFailed()) {
                failedList.add(itemResponse.getFailure());
            }
        }
        return failedList;
    }

}
