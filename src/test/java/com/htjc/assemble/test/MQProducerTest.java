package com.htjc.assemble.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.htjc.assemble.util.ConfigUtil;
import org.apache.commons.collections.map.HashedMap;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.fail;

/**
 * Created by guilin on 2016/9/7.
 */
public class MQProducerTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(MQProducerTest.class);

    private static DefaultMQProducer producer = null;

    private static String topic = null;

    @BeforeClass
    public static void beforeClass() {
        producer = new DefaultMQProducer();
        //ProducerGroup这个概念发送普通的消息时，作用不大，但是发送分布式事务消息时，比较关键，因为服务器会回查这个Group下的任意一个Producer
        producer.setProducerGroup("producer1");
        String namesrvAddr = ConfigUtil.getConfig("mq").getString("namesrv");
        producer.setNamesrvAddr(namesrvAddr);
        try {
            producer.start();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        topic = ConfigUtil.getConfig("mq").getString("topic");
    }

    @AfterClass
    public static void afterClass() {
        if (producer != null) {
            producer.shutdown();
        }
    }

    /**
     * 包含身份证号的消息
     */
    @Test
    public void testIDCardMsg() throws Exception {
        Map<String, Object> source = new HashedMap();
        source.put("idcard", "411323198308012652");
        Message message = new Message(topic, "idcard", "idcard1", JSON.toJSONBytes(source));
        producer.send(message, 1000 * 10);
        logger.info(JSON.toJSONString(source));
    }

    /**
     * 包含IP地址的消息
     *
     * @throws Exception
     */
    @Test(timeout = 30 * 1000)
    public void testIpMsg() throws Exception {
        String[] ips = {"93.123.23.31", "93.123.23.38", "93.123.23.57", "197.199.253.57", "197.199.253.6"
                , "197.199.253.32", "197.199.253.13", "197.199.253.18", "93.123.23.28", "93.123.23.19"};
        Random random = new Random();

        for (int j = 0; j < 5; j++) {
            Map<String, Object> source = new HashMap<>();
            source.put("CREATE_TIME", DateTime.now().toString("yyyyMMddHHmmssSSS"));
            source.put("userIP", ips[random.nextInt(10)]);

            Message msg = new Message(topic,//topic
                    "ZFPT_HX:HX_INDIVIDUAL_USER_INFO",//tag
                    "ip-" + j + "",//key
                    JSON.toJSONBytes(source)
            );
            producer.send(msg);
            logger.info(JSON.toJSONString(source));
        }
    }

    /**
     * 一般消息，DEFAULT工序链处理
     *
     * @throws Exception
     */
    @Test
    public void testCommonMsg() throws Exception {
        List<Message> messageList = new ArrayList<>();

        Map<String, Object> source = new HashedMap();
        source.put("CREATE_TIME", DateTime.now().toString("yyyyMMddHHmmssSSS"));
        source.put("msg", "hello world 1");
        Message msg = new Message(topic, null, "c1", JSON.toJSONBytes(source));
        messageList.add(msg);
        logger.info(JSON.toJSONString(source));

        source = new HashedMap();
        source.put("msg", "hello world 2");
        msg = new Message(topic, null, "c2", JSON.toJSONBytes(source));
        messageList.add(msg);
        logger.info(JSON.toJSONString(source));

        source = new HashedMap();
        source.put("msg", "hello world 3");
        source.put("null_TIME", null);
        source.put("NULL_TIME", "NULL");
        source.put("blank_TIME", " ");
        source.put("UPDATE_TIME", System.currentTimeMillis());
        msg = new Message(topic, null, "c3", JSON.toJSONBytes(source));
        messageList.add(msg);
        logger.info(JSON.toJSONString(source));

        for (Message message : messageList) {
            producer.send(message);
        }
    }

}
