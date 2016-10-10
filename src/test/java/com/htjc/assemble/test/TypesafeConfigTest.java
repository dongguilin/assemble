package com.htjc.assemble.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

/**
 * Created by guilin on 2016/9/19.
 */
public class TypesafeConfigTest {

    @Test
    public void testParseStr() {
        String str = "mq {\n" +
                "  #nameserver地址列表，多个地址以\",\"作为分隔符\n" +
                "  namesrv: \"${mq.namesrv}\"\n" +
                "  #消费的主题\n" +
                "  topic: \"${mq.topic}\"\n" +
                "  tags: \"*\"\n" +
                "  #批量拉消息，一次最多拉多少条\n" +
                "  pullBathSize: 1000\n" +
                "  #consumer组名\n" +
                "  consumerGroup: \"${mq.consumerGroup}\"\n" +
                "}";

        Config config = ConfigFactory.parseReader(new StringReader(str));
        Assert.assertEquals(1000, config.getInt("mq.pullBathSize"));
        Assert.assertEquals("1000", config.getString("mq.pullBathSize"));
        Assert.assertEquals("${mq.consumerGroup}", config.getString("mq.consumerGroup"));

    }

    @Test
    public void testParseStr2() {
        String str = "{\n" +
                "  #nameserver地址列表，多个地址以\",\"作为分隔符\n" +
                "  namesrv: \"${mq.namesrv}\"\n" +
                "  #消费的主题\n" +
                "  topic: \"${mq.topic}\"\n" +
                "  tags: \"*\"\n" +
                "  #批量拉消息，一次最多拉多少条\n" +
                "  pullBathSize: 1000\n" +
                "  #consumer组名\n" +
                "  consumerGroup: \"${mq.consumerGroup}\"\n" +
                "}";
        Config config = ConfigFactory.parseReader(new StringReader(str));
        System.out.println(config);
    }


}
