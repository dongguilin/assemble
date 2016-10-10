package com.htjc.assemble.test;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by guilin on 2016/9/7.
 * elasticsearch测试
 */
public class EsTest extends EsBase {

    private static final Logger logger = LoggerFactory.getLogger(EsTest.class);

    @Test
    public void testInsertOneDoc() {
        BulkRequestBuilder builder = esClient.prepareBulk();
        IndexRequest request = esClient.prepareIndex("documents20", "doc").request();
        Map<String, Object> map = buildData();
        byte[] buff = JSON.toJSONBytes(map);
        logger.info("size:{} 字节", buff.length);
        request.source(buff);
        builder.add(request);
        BulkResponse response = builder.execute().actionGet();
    }

    @Test
    public void testInsertTimeField() {
        BulkRequestBuilder builder = esClient.prepareBulk();

        String[] arr = {"20160929", "20160929142434", "20160929142434111", "2016-09-29", "2016-09-29 14:24:34"};

        for (int i = 0; i < arr.length; i++) {
            IndexRequest request = esClient.prepareIndex("monitor-2016-10", "doc").request();
            Map<String, Object> source = new HashedMap();
            source.put("id", i + "");
            source.put("BANK_PROCESS_TIME", arr[i]);
            request.source(JSON.toJSONBytes(source));
            builder.add(request);
        }

        builder.execute().actionGet();
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


}
