package com.htjc.assemble.test;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by guilin on 2016/9/7.
 */
public class MQPushConsumerTest {

    private static final Logger logger = LoggerFactory.getLogger(MQPushConsumerTest.class);

    private String namesrvAddr = "192.168.60.123:9876";

    /**
     * 集群消费条件：consumerGroup相同、messageModel=MessageModel.CLUSTERING、instanceName不同
     * <p>
     * 同一jvm中无法运行多个instanceName相同的consumer
     * 不同jvm相同instanceName的consumer接收到的消息是相同的
     *
     * @throws Exception
     */
    @Test
    public void testCluster() throws Exception {
        int consumerNum = 1;
        for (int i = 0; i < consumerNum; i++) {
            final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("pushConsumerGroup");
            consumer.setNamesrvAddr(namesrvAddr);
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.subscribe("log-monitor", "*");

            consumer.setInstanceName("guilin" + i);

            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    logger.info("instanceName:{} thread:{} queueId:{} msgNum:{}", consumer.getInstanceName(),
                            Thread.currentThread().getName(), context.getMessageQueue().getQueueId(), msgs.size());
                    for (MessageExt msg : msgs) {
                        logger.info("instanceName:{} id:{} ", consumer.getInstanceName(), msg.getKeys());
                    }
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

            consumer.start();
            logger.info("start consumer:{}", consumer);
        }

        Thread.currentThread().join();
    }

}
