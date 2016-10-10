package com.htjc.assemble.util;

import com.htjc.configs.base.util.ConfigsUtil;
import com.htjc.configs.client.main.LoaderFromEnv;
import com.htjc.configs.watcher.action.Action;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by guilin on 2016/9/1.
 */
public class ConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static Config localConfig = null;

    //是否启用配置中心
    private static boolean useConfigCenter;

    //支持的工序
    private static Map<String, Class> procedures;


    public static void loadConfig(CommandLine commandLine) throws Exception {
        Properties properties = getConfigCenterProp();

        //是否启用配置中心
        if (commandLine.hasOption("cc")) {
            useConfigCenter = commandLine.getOptionValue("cc").equals("false") ? false : true;
        } else {
            useConfigCenter = Boolean.parseBoolean(properties.getProperty("enable", "true"));
        }

        if (useConfigCenter) {
            logger.info("load configs from config-center");
            loadFromConfigCenter(properties.getProperty("env", "HTJC_CONFIG_HOME"),
                    properties.getProperty("appName", "assemble"), properties.getProperty("versionNo"));
        } else {
            //加载配置文件
            if (commandLine.hasOption("f")) {
                File configurationFile = new File(commandLine.getOptionValue('f'));
                String path = configurationFile.getPath();
                try {
                    path = configurationFile.getCanonicalPath();
                    logger.info("load config file:{}", path);
                    ConfigUtil.load(path);
                } catch (IOException e) {
                    logger.error("Failed to read canonical path for file: {}", path, e);
                    throw e;
                }
            } else {
                logger.info("load config file:application.conf");
                ConfigUtil.load();
            }
        }

        //加载工序链配置
        ConfigUtil.loadProcedures();
    }

    /**
     * 加载config-center.properties文件
     */
    private static Properties getConfigCenterProp() throws IOException {
        Properties properties = new Properties();
        String fileName = "config-center.properties";
        String path = ConfigUtil.class.getClassLoader().getResource(fileName).getPath();
        try {
            properties.load(new FileInputStream(new File(path)));
        } catch (IOException e) {
            logger.error("load {} error", fileName, e);
            throw e;
        }
        return properties;
    }

    /**
     * 加载配置中心
     *
     * @param env
     * @param appName
     * @param versionNo
     */
    private static void loadFromConfigCenter(String env, String appName, String versionNo) {
        new LoaderFromEnv(env).init(appName, versionNo, new Action() {
            @Override
            public void beforeExecAction(Map<String, Object> map) {

            }

            @Override
            public void afterExecAction(Map<String, Object> map) {

            }
        });
    }

    public static Config getConfig(String key) {
        if (useConfigCenter) {
            try {
                String value = ConfigsUtil.getString(key);
                return ConfigFactory.parseReader(new StringReader(value));
            } catch (Exception e) {
                logger.error("parse config error, key:{}", key, e);
                throw new RuntimeException(e);
            }
        }
        return localConfig.getConfig(key);
    }

    public static List<? extends Config> getConfigList(String key) {
        if (useConfigCenter) {
            try {
                String value = ConfigsUtil.getString(key);
                return ConfigFactory.parseReader(new StringReader(key + ":" + value)).getConfigList(key);
            } catch (Exception e) {
                logger.error("parse config error, key:{}", key, e);
                throw new RuntimeException(e);
            }
        }
        return localConfig.getConfigList(key);
    }


    private static void load(String path) {
        localConfig = ConfigFactory.parseFile(new File(path));
    }

    private static void load() {
        localConfig = ConfigFactory.load();
    }

    private static void loadProcedures() throws ClassNotFoundException {
        Config config = getConfig("procedures");
        procedures = new HashMap<>();
        Set<Map.Entry<String, ConfigValue>> set = config.entrySet();
        Iterator<Map.Entry<String, ConfigValue>> iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConfigValue> entry = iterator.next();
            String classfullName = entry.getValue().unwrapped().toString();
            try {
                procedures.put(entry.getKey(), Class.forName(classfullName));
            } catch (ClassNotFoundException e) {
                logger.error("工序配置有误", e);
                throw e;
            }
        }
    }

    public static Map<String, Class> getProcedures() {
        return procedures;
    }

}
