package com.htjc.assemble.procedure;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.htjc.assemble.util.ConfigConstants.*;

/**
 * Created by guilin on 2016/9/27.
 * 默认工序链
 */
public class DefaultProcedureChain extends AbstractProcedureChain {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProcedureChain.class);

    private Config config;

    public DefaultProcedureChain(Config config) {
        this.config = config;
    }

    @Override
    public void buildChain() {
        //工序链名称
        super.name = config.getString(RULES_NAME);

        logger.info("######build procedure chain:{}######", super.name);

        //工序链配置
        buildProcedures(config.getConfigList(RULES_PROCEDURES));
    }

    private void buildProcedures(List<? extends Config> procedureList) {
        super.procedures = Lists.newLinkedList();
        for (Config tmp : procedureList) {
            String produreName = tmp.getString(RULES_PROCEDURES_NAME);
            try {
                Procedure procedure = ProcedureFactory.newInstance(produreName);
                procedure.setName(produreName);

                try {
                    Config configs = tmp.getConfig(RULES_PROCEDURES_CONFIG);
                    Properties properties = new Properties();
                    Iterator<Map.Entry<String, ConfigValue>> iterator = configs.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, ConfigValue> entry = iterator.next();
                        properties.put(entry.getKey(), entry.getValue().unwrapped().toString());
                    }
                    procedure.setConfig(properties);
                } catch (ConfigException e) {
                }

                procedures.add(procedure);
            } catch (ClassNotFoundException e) {
                logger.error("Procedure class not found. Exception follows.", e);
                throw new RuntimeException("Procedure not found.", e);
            } catch (IllegalAccessException e) {
                logger.error("Could not instantiate Procedure Exception follows.", e);
                throw new RuntimeException("Procedure not constructable.", e);
            } catch (InstantiationException e) {
                logger.error("Unable to access Procedure Exception follows.", e);
                throw new RuntimeException("Unable to access Procedure.", e);
            }
        }
    }
}
