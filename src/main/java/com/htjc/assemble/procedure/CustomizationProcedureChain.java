package com.htjc.assemble.procedure;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.htjc.assemble.util.ConfigConstants.*;

/**
 * Created by guilin on 2016/9/1.
 * 定制化工序链，配置拷贝自默认工序链，然后可在此基础上定制化配置，覆盖
 */
public class CustomizationProcedureChain extends AbstractProcedureChain {

    private static final Logger logger = LoggerFactory.getLogger(CustomizationProcedureChain.class);

    //默认工序链
    private final DefaultProcedureChain defaultProcedureChain;

    private Config config;

    public CustomizationProcedureChain(Config config, DefaultProcedureChain defaultProcedureChain) {
        this.config = config;
        this.defaultProcedureChain = defaultProcedureChain;
    }

    @Override
    public void buildChain() {
        super.name = config.getString(RULES_NAME);

        Preconditions.checkArgument(!super.name.equals(DEFAULT_PROCEDURES_NAME), "已经存在默认工序链");

        logger.info("######build procedure chain:{}######", super.name);

        buildProcedures(config.getConfigList(RULES_PROCEDURES));
    }

    private void buildProcedures(List<? extends Config> procedureList) {
        Map<String, Procedure> defaultProcedures = defaultProcedureMap(defaultProcedureChain.procedures);

        super.procedures = Lists.newLinkedList();

        for (Config tmp : procedureList) {
            String produreName = tmp.getString(RULES_PROCEDURES_NAME);
            try {
                Procedure procedure = ProcedureFactory.newInstance(produreName);
                procedure.setName(produreName);

                Properties config = null;
                if (defaultProcedures.containsKey(produreName)) {
                    config = defaultProcedures.get(produreName).getConfig();
                } else {
                    config = new Properties();
                }

                try {
                    Config configs = tmp.getConfig(RULES_PROCEDURES_CONFIG);
                    Iterator<Map.Entry<String, ConfigValue>> iterator = configs.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, ConfigValue> entry = iterator.next();
                        config.put(entry.getKey(), entry.getValue().unwrapped().toString());
                    }
                } catch (ConfigException e) {
                }

                procedure.setConfig(config);
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
            } catch (Exception e) {
                throw new RuntimeException(tmp.toString(), e);
            }
        }
    }

    private Map<String, Procedure> defaultProcedureMap(final List<Procedure> procedures) {
        Map<String, Procedure> map = new LinkedHashMap<>();
        for (Procedure p : procedures) {
            map.put(p.getName(), p);
        }
        return map;
    }
}
