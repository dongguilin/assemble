package com.htjc.assemble;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.htjc.assemble.config.CliOptions;
import com.htjc.assemble.util.ConfigUtil;
import com.typesafe.config.Config;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by guilin on 2016/9/7.
 */
public class Consumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    //mq消费者作为输入来源
    private DefaultMQPushConsumer pushConsumer;

    public Consumer() {
        initConsumer();
    }

    private void initConsumer() {
        pushConsumer = new DefaultMQPushConsumer();

        Properties properties = CliOptions.getProperties();
        Config config = ConfigUtil.getConfig("mq");
        pushConsumer.setNamesrvAddr(properties.getProperty("mq.namesrv", config.getString("namesrv")));

        String consumerGroup = properties.getProperty("mq.consumerGroup", config.getString("consumerGroup"));
        pushConsumer.setConsumerGroup(consumerGroup);

        pushConsumer.setMessageModel(MessageModel.CLUSTERING);

        //最小线程数
        int consumeThreadMin = Integer.parseInt(properties.getProperty("mq.consumeThreadMin", config.getString("consumeThreadMin")));
        pushConsumer.setConsumeThreadMin(consumeThreadMin);
        //最大线程数
        int consumeThreadMax = Integer.parseInt(properties.getProperty("mq.consumeThreadMax", config.getString("consumeThreadMax")));
        pushConsumer.setConsumeThreadMax(consumeThreadMax);

        int pullBathSize = Integer.parseInt(properties.getProperty("mq.pullBathSize", config.getString("pullBathSize")));
        pushConsumer.setPullBatchSize(pullBathSize);//批量拉消息，一次最多拉多少条
        pushConsumer.setConsumeMessageBatchMaxSize(pullBathSize);//批量消费，一次最多消费多少条

        try {
            String topic = properties.getProperty("mq.topic", config.getString("topic"));
            String tags = null;
            if (properties.containsKey("mq.tags")) {
                tags = properties.getProperty("mq.tags");
            } else {
                tags = config.getString("tags");
            }
            if (StringUtils.isBlank(tags)) {
                tags = "*";
            }
            pushConsumer.subscribe(topic, tags);
            logger.info("subscribe topic:{} tags:{}", topic, tags);
        } catch (MQClientException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        pushConsumer.registerMessageListener(new MsgHandler());
    }

    public void start() throws MQClientException {
        pushConsumer.start();
        logger.info("start consumer:{} success", pushConsumer);
    }

    public void shutDown() {
        if (pushConsumer != null) {
            pushConsumer.shutdown();
            logger.info("shutdown consumer:{} success", pushConsumer);
        }
    }

}
