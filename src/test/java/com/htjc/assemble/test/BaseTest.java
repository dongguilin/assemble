package com.htjc.assemble.test;

import com.htjc.assemble.config.CliOptions;
import com.htjc.assemble.util.ConfigUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import static org.junit.Assert.fail;

/**
 * Created by guilin on 2016/10/8.
 */
public class BaseTest {

    static {
        try {
            CommandLine commandLine = CliOptions.initOptions(new Options(), new String[0]);
            ConfigUtil.loadConfig(commandLine);
        } catch (Exception e) {
            fail(e.getMessage());
            System.exit(-1);
        }
    }
}
