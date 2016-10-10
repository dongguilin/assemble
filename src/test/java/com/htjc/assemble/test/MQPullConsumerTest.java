package com.htjc.assemble.test;

import com.alibaba.rocketmq.client.consumer.DefaultMQPullConsumer;
import com.alibaba.rocketmq.client.consumer.PullResult;
import com.alibaba.rocketmq.client.consumer.PullStatus;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.common.protocol.heartbeat.MessageModel;
import com.google.common.base.Preconditions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by guilin on 2016/9/7.
 */
public class MQPullConsumerTest {

    private static final Logger logger = LoggerFactory.getLogger(MQPullConsumerTest.class);

    private String namesrvAddr = "192.168.60.123:9876";

    /**
     * 运行异常，不建议用，可使用DefaultMQPushConsumer进行集群消费
     * 每个consumer消费的是全部消息，没有负载均衡，另外consumer有时会重复消费消息
     *
     * @throws Exception
     */
    @Test
    public void testCluster() throws Exception {
        final String topic = "log-monitor";
        final String tags = "*";
        final int pullBathSize = 32;

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

        class Job implements Runnable {

            private DefaultMQPullConsumer consumer;

            public Job(DefaultMQPullConsumer consumer) {
                this.consumer = consumer;
            }

            @Override
            public void run() {
                try {

                    Set<MessageQueue> mqs = Preconditions.checkNotNull(consumer.fetchSubscribeMessageQueues(topic));

//                    Set<MessageQueue> mqs = Preconditions.checkNotNull(consumer.fetchMessageQueuesInBalance(topic));

                    // 必须加上此监听才能在消费过后，自动回写消费进度
                    consumer.registerMessageQueueListener(topic, null);

                    for (MessageQueue mq : mqs) {
                        // 获取offset //TODO fromstore?
                        long offset = consumer.fetchConsumeOffset(mq, false);
                        offset = offset < 0 ? 0 : offset;
//                        PullResult pullResult = consumer.pull(mq, tags, offset, pullBathSize);
                        PullResult pullResult = consumer.pullBlockIfNotFound(mq, tags, offset, pullBathSize);

                        PullStatus status = pullResult.getPullStatus();

                        //新的消息
                        if (status == PullStatus.FOUND) {
                            List<MessageExt> messageExts = pullResult.getMsgFoundList();
                            logger.info("instanceName:{} thread:{} queueId:{} msgNum:{}", consumer.getInstanceName(),
                                    Thread.currentThread().getName(), mq.getQueueId(), messageExts.size());
                            for (MessageExt messageExt : messageExts) {
                                logger.info("instanceName:{} id:{}", consumer.getInstanceName(), messageExt.getKeys());
                            }
                            //更新offset
                            consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        int concumerNum = 2;
        for (int i = 0; i < concumerNum; i++) {
            final DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("pullConsumerGroup");
            consumer.setNamesrvAddr(namesrvAddr);
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.setInstanceName("guilin" + i);
            consumer.start();

            logger.info("start consumer:{}", consumer);

            pool.scheduleWithFixedDelay(new Job(consumer), 0, 10, TimeUnit.MICROSECONDS);
        }

        Thread.currentThread().join();
    }
}
