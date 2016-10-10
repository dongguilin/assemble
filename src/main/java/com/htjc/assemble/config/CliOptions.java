package com.htjc.assemble.config;

import org.apache.commons.cli.*;

import java.util.Properties;

/**
 * Created by guilin on 2016/9/6.
 * 命令行配置参数
 */
public class CliOptions {

    private static Properties properties = null;

    private static CommandLine commandLine = null;

    public static CommandLine initOptions(Options options, String[] args) throws ParseException {

        Option option = new Option("h", "help", false, "display help text");
        options.addOption(option);

        option = new Option("f", "conf-file", true,
                "specify a config file");
        option.setRequired(false);
        options.addOption(option);

        //是否使用配置中心，默认true，即使用，false表示使用application.conf文件的配置
        option = new Option("cc", "config-center", true, "use config-center? default is true");
        option.setRequired(false);
        options.addOption(option);

        options.addOption(OptionBuilder.withDescription("use value for given property")
                .hasArgs()
                .withValueSeparator()
                .create('D'));

        CommandLineParser parser = new GnuParser();
        commandLine = parser.parse(options, args);

        return commandLine;
    }

    public static Properties getProperties() {
        if (properties == null) {
            properties = commandLine.getOptionProperties("D");
        }
        return properties;
    }
}
