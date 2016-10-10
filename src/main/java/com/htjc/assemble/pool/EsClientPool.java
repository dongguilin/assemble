package com.htjc.assemble.pool;

import com.htjc.assemble.config.CliOptions;
import com.htjc.assemble.util.ConfigUtil;
import com.typesafe.config.Config;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.shield.ShieldPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by guilin on 2016/9/18.
 * elasticsearch连接池工具类
 */
public class EsClientPool {

    private static final Logger logger = LoggerFactory.getLogger(EsClientPool.class);

    private static GenericObjectPool<Client> pool;

    static {
        Properties properties = CliOptions.getProperties();
        Config config = ConfigUtil.getConfig("es");
        GenericObjectPoolConfig cfg = new GenericObjectPoolConfig();
        int maxTotal = Integer.parseInt(properties.getProperty("es.pool.maxTotal", config.getString("pool.maxTotal")));
        cfg.setMaxTotal(maxTotal);//最大连接数
        int maxIdle = Integer.parseInt(properties.getProperty("es.pool.maxIdle", config.getString("pool.maxIdle")));
        cfg.setMaxIdle(maxIdle);//最大空闲连接数
        long maxWaitMillis = Long.parseLong(properties.getProperty("es.pool.maxWaitMills", config.getString("pool.maxWaitMills")));
        cfg.setMaxWaitMillis(maxWaitMillis);//当连接池资源耗尽时，调用者最大阻塞的时间，超时将抛出异常
        cfg.setMinEvictableIdleTimeMillis(10 * 60000L); // 连接空闲的最小时间，达到此值后空闲连接将可能会被移除
        cfg.setTestOnBorrow(true);//向调用者输出“连接”资源时，是否检测是有有效，如果无效则从连接池中移除，并尝试获取继续获取
        cfg.setTimeBetweenEvictionRunsMillis(1 * 60000L); // “空闲连接”检测线程，检测的周期
        pool = new GenericObjectPool<Client>(new EsClientPooledFactory(), cfg);
        logger.info("init elasticsearch pool success");
    }

    public static Client borrowObject() throws Exception {
        return pool.borrowObject();
    }

    public static Client borrowObject(long borrowMaxWaitMillis) throws Exception {
        return pool.borrowObject(borrowMaxWaitMillis);
    }

    public static void returnObject(Client client) {
        if (client != null) {
            pool.returnObject(client);
        }
    }

    public static void closePool() {
        if (pool != null) {
            pool.close();
            logger.info("close elasticsearch pool success");
        }
    }

    private static class EsClientPooledFactory extends BasePooledObjectFactory<Client> {
        @Override
        public Client create() throws Exception {
            return buildEsClient();
        }

        @Override
        public PooledObject<Client> wrap(Client client) {
            return new DefaultPooledObject<Client>(client);
        }

        @Override
        public void destroyObject(PooledObject<Client> p) throws Exception {
            p.getObject().close();
        }

        /**
         * Build elasticsearch client
         *
         * @return
         */
        private Client buildEsClient() {

            Properties properties = CliOptions.getProperties();

            Config esConfig = ConfigUtil.getConfig("es");

            Settings.Builder builder = Settings.settingsBuilder()
                    .put("cluster.name", properties.getProperty("es.cluster", esConfig.getString("cluster")));


            boolean enableShield = Boolean.parseBoolean(properties.getProperty("es.shield.enabled", esConfig.getString("shield.enabled")));
            if (enableShield) {
                String user = properties.getProperty("es.shield.user", esConfig.getString("shield.user"));
                String pwd = properties.getProperty("es.shield.pwd", esConfig.getString("shield.pwd"));
                builder.put("shield.user", user + ":" + pwd);
            }
            Settings settings = builder.build();

            TransportClient.Builder tBuilder = TransportClient.builder();
            if (enableShield) {
                tBuilder.addPlugin(ShieldPlugin.class);
            }
            TransportClient transportClient = tBuilder.settings(settings).build();

            String[] hosts = properties.getProperty("es.hosts", esConfig.getString("hosts")).split(",");
            for (String host : hosts) {
                String[] arr = host.split(":");
                transportClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(arr[0], Integer.parseInt(arr[1]))));
            }
            return transportClient;
        }
    }

}
