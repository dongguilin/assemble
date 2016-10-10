package com.htjc.assemble.test;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by guilin on 2016/9/8.
 */
public class EsBatchInsertTest extends EsBase {

    private static final Logger logger = LoggerFactory.getLogger(EsBatchInsertTest.class);

    @Test
    public void testBatchInsert() {
        logger.info("//////////////////////////////////////////////////////////////");
        int times = 5;//次数

        int totalRec = 10000;//总记录数
        int pageSize = 10000;//每个批次数量

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
                BulkRequestBuilder builder = esClient.prepareBulk();
                for (int i = 0; i < pageSize; i++) {
                    IndexRequest request = esClient.prepareIndex("documents20", "doc").request();
                    request.source(source);
                    builder.add(request);
                }
                long buildTimeEnd = System.currentTimeMillis();//构建数据 end time

                totalBuildDataTime = totalBuildDataTime + (buildTimeEnd - buildTimeStart);

                long esReqTimeStart = System.currentTimeMillis();//发送es请求 start time
                BulkResponse response = builder.execute().actionGet();
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
