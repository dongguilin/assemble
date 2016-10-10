package com.htjc.assemble.test;

import com.htjc.assemble.config.CliOptions;
import com.htjc.assemble.util.ConfigUtil;
import com.typesafe.config.Config;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by guilin on 2016/9/19.
 * 配置中心测试
 */
public class ConfigCenterConfigTest {

    @Test
    public void test1() throws Exception {
        CommandLine commandLine = CliOptions.initOptions(new Options(), new String[]{"-cc", "true"});
        ConfigUtil.loadConfig(commandLine);

        Config esConfig = ConfigUtil.getConfig("es");
        System.out.println(esConfig.getString("hosts"));
        assertNotNull(esConfig.getString("hosts"));

        List list = ConfigUtil.getConfigList("rules");
        assertTrue(list.size() > 0);
    }
}
