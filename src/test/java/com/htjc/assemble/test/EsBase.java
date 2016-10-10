package com.htjc.assemble.test;

import com.htjc.assemble.config.CliOptions;
import com.htjc.assemble.util.ConfigUtil;
import com.typesafe.config.Config;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.shield.ShieldPlugin;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by guilin on 2016/9/7.
 */
public class EsBase extends BaseTest {
    protected static Client esClient;

    @BeforeClass
    public static void beforeClass() {
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

        esClient = transportClient;
    }

    @AfterClass
    public static void afterClass() {
        if (esClient != null) {
            esClient.close();
        }
    }
}
