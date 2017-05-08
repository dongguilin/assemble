package com.htjc.assemble.procedure;

import java.util.Properties;

/**
 * Created by guilin on 2016/9/1.
 * 抽象工序
 */
public abstract class AbstractProcedure implements Procedure {
    //名称
    protected String name;
    //配置
    protected Properties config;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Properties getConfig() {
        return config;
    }

    @Override
    public void setConfig(Properties config) throws Exception {
        this.config = config;
    }

    @Override
    public String toString() {
        return "AbstractProcedure{" +
                "name='" + name + '\'' +
                ", config=" + config +
                '}';
    }
}
