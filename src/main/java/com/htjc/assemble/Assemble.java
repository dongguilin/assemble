package com.htjc.assemble;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.htjc.assemble.config.CliOptions;
import com.htjc.assemble.pool.EsClientPool;
import com.htjc.assemble.util.ConfigUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by guilin on 2016/9/7.
 */
public class Assemble {

    private static final Logger logger = LoggerFactory.getLogger(Assemble.class);

    public static void main(String[] args) throws Exception {

        //命令行配置
        Options options = new Options();
        CommandLine commandLine = null;
        commandLine = CliOptions.initOptions(options, args);
        if (commandLine.hasOption('h')) {//打印命令使用提示信息
            new HelpFormatter().printHelp("assemble", options, true);
            return;
        }

        //加载配置(从配置中心或本地配置文件加载，命令行可指定参数)
        ConfigUtil.loadConfig(commandLine);

        //mq消费者
        final Consumer consumer = new Consumer();
        try {
            consumer.start();
        } catch (MQClientException e) {
            logger.error("consumer start error", e);
            System.exit(0);
        }

        //关闭钩子,释放资源
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                consumer.shutDown();
                EsClientPool.closePool();
            }
        });

        logger.info("start success");
    }
}
