package com.htjc.assemble.test;

import com.htjc.assemble.config.ProcedureConfig;
import com.htjc.assemble.procedure.AbstractProcedureChain;
import org.junit.Test;

import java.util.Map;

/**
 * Created by guilin on 2016/9/29.
 */
public class ConfigTest extends BaseTest {

    @Test(expected = UnsupportedOperationException.class)
    public void testChainMap() {
        Map<String, AbstractProcedureChain> chainMap = ProcedureConfig.buildChainMap();
        chainMap.put("DEFAULT", new AbstractProcedureChain() {
            @Override
            public void buildChain() {

            }
        });
    }

}
