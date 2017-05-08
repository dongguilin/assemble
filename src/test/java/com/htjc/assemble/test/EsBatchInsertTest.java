package com.htjc.assemble.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htjc.assemble.pool.EsRestClientPool;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by guilin on 2016/9/8.
 */
public class EsBatchInsertTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(EsBatchInsertTest.class);

    private List<RestClient> restClientList;

    @Before
    public void before() throws Exception {
        restClientList = EsRestClientPool.borrowObject();
    }

    @After
    public void after() {
        EsRestClientPool.returnObject(restClientList);
    }


    @Test
    public void testBatchInsert() throws IOException {
        logger.info("//////////////////////////////////////////////////////////////");
        int times = 5;//次数

        int totalRec = 1000;//总记录数
        int pageSize = 1000;//每个批次数量

        List<Long> totalList = new ArrayList<>(times);
        List<Long> buildList = new ArrayList<>(times);
        List<Long> requestList = new ArrayList<>(times);

        Map<String, Object> map = buildData();
        byte[] source = JSON.toJSONBytes(map);

        for (int t = 0; t < times; t++) {
            long start = System.currentTimeMillis();
            long totalBuildDataTime = 0;
            long totalRequestTime = 0;

            for (int p = 0; p < totalRec / pageSize; p++) {
                logger.info("第{}次 第{}页", (t + 1), (p + 1));


                long buildTimeStart = System.currentTimeMillis();//构建数据 start time
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < pageSize; i++) {
                    Map<String, Object> datamap = new HashMap<>();
                    datamap.put("_index", "document20");
                    datamap.put("_type", "doc");
//                  datamap.put("_id", doc.getId());
                    buffer.append(JSON.toJSONString(Collections.singletonMap("index", datamap))).append("\n");
                    buffer.append(JSON.toJSONString(buildData())).append("\n");
                }

                long buildTimeEnd = System.currentTimeMillis();//构建数据 end time

                totalBuildDataTime = totalBuildDataTime + (buildTimeEnd - buildTimeStart);

                long esReqTimeStart = System.currentTimeMillis();//发送es请求 start time
                HttpEntity entity = new NStringEntity(buffer.toString(), ContentType.APPLICATION_JSON);
                for (RestClient restClient : restClientList) {
                    Response response = restClient.performRequest("POST", "/_bulk", Collections.<String, String>emptyMap(), entity);
                    String str = IOUtils.readLines(response.getEntity().getContent()).get(0);
                    JSONObject rootObj = JSON.parseObject(str);
                    long took = rootObj.getLong("took");
                    boolean hasFailures = rootObj.getBoolean("errors");
                    logger.info("took:{}, hasFailures:{}", took, hasFailures);

                }
                long esReqTimeEnd = System.currentTimeMillis();//接收到es回复 end time

                totalRequestTime = totalRequestTime + (esReqTimeEnd - esReqTimeStart);
            }

            long end = System.currentTimeMillis();

            totalList.add(end - start);
            buildList.add(totalBuildDataTime);
            requestList.add(totalRequestTime);
        }

        logger.info("单条记录:{} 字节", source.length);
        logger.info("总记录数:{} 每个批次记录数:{} 测试总次数:{} 每次平均耗费{}毫秒", totalRec, pageSize, times, avg(totalList));
        logger.info("构建数据平均耗费{}毫秒", avg(buildList));
        logger.info("入es平均耗费{}毫秒", avg(requestList));

    }

    private Map<String, Object> buildData() {
        Properties properties = System.getProperties();
        Map<String, Object> map = new HashedMap(properties.size());
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> entry = iterator.next();
            String key = entry.getKey().toString().replace(".", "");
            map.put(key, entry.getValue());
        }
        return map;
    }

    private long avg(List<Long> list) {
        long total = 0;
        for (Long l : list) {
            total += l;
        }
        return total / list.size();
    }
}
