package com.htjc.assemble.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;

/**
 * Created by guilin on 2016/9/9.
 */
public class MyTest {

    @Test
    public void test1() throws ParseException {


    }

    @Test
    public void test2() throws IOException, MQClientException, RemotingException, InterruptedException, MQBrokerException {
        final DefaultMQProducer producer = new DefaultMQProducer();
//        producer.setNamesrvAddr("192.168.70.104:9876");
        producer.setNamesrvAddr("192.168.60.123:9876");
        producer.setProducerGroup("producer1");
        producer.start();

        //////////////////////
        File file = new File(MyTest.class.getClassLoader().getResource("").getPath() + "/1.csv");
        CSVParser csvParser = CSVParser.parse(file, Charset.defaultCharset(), CSVFormat.DEFAULT);
        System.out.println(csvParser.getHeaderMap());

        List<CSVRecord> list = csvParser.getRecords();

        List<String> headers = new LinkedList<>();
        CSVRecord header = list.get(0);
        Iterator<String> iterator = header.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
//            if (StringUtils.isNotBlank(value)) {
            headers.add(value);
//            }
        }
        System.out.println(headers);


        for (int i = 1; i < list.size(); i++) {
            CSVRecord record = list.get(i);
            List<String> list2 = new LinkedList<>();
            Iterator<String> iterator2 = record.iterator();
            while (iterator2.hasNext()) {
                list2.add(iterator2.next());
            }
//            System.out.println(list2);

            Map<String, Object> map = new HashMap<>();
            for (int j = 1; j < headers.size(); j++) {
                map.put(headers.get(j), list2.get(j));
            }

//            System.out.println(map);
            for (int j = 0; j < 10; j++) {
                Message msg = new Message("log-monitor", null, String.valueOf(j), JSON.toJSONBytes(map));
                producer.send(msg);
            }
        }

        producer.shutdown();


    }

}
