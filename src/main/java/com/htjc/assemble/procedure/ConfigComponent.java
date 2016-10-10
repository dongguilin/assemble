package com.htjc.assemble.procedure;

import java.util.Properties;

/**
 * Created by guilin on 2016/9/28.
 * 配置接口
 */
public interface ConfigComponent {
    void setConfig(Properties config);

    Properties getConfig();
}
