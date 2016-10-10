package com.htjc.assemble.config;

import com.htjc.assemble.procedure.AbstractProcedureChain;
import com.htjc.assemble.procedure.CustomizationProcedureChain;
import com.htjc.assemble.procedure.DefaultProcedureChain;
import com.htjc.assemble.util.ConfigUtil;
import com.typesafe.config.Config;
import org.apache.commons.collections.MapUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.htjc.assemble.util.ConfigConstants.*;

/**
 * Created by guilin on 2016/9/1.
 * 工序配置
 */
public class ProcedureConfig {

    public static Map<String, AbstractProcedureChain> buildChainMap() {
        Map<String, AbstractProcedureChain> procedureChainMap = new LinkedHashMap<>();

        List<? extends Config> list = ConfigUtil.getConfigList(RULES);
        for (Config config : list) {
            String name = config.getString(RULES_NAME);

            //默认工序链
            if (name.equals(DEFAULT_PROCEDURES_NAME)) {
                DefaultProcedureChain chain = new DefaultProcedureChain(config);
                chain.buildChain();
                procedureChainMap.put(name, chain);
            } else {
                CustomizationProcedureChain chain = new CustomizationProcedureChain(config,
                        (DefaultProcedureChain) procedureChainMap.get(DEFAULT_PROCEDURES_NAME));
                chain.buildChain();
                procedureChainMap.put(name, chain);
            }
        }
        return MapUtils.unmodifiableMap(procedureChainMap);
    }

}
