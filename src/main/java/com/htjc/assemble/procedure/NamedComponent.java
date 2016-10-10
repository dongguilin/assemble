package com.htjc.assemble.procedure;

/**
 * Created by guilin on 2016/9/28.
 * 名称接口
 * Enables a component to be tagged with a name so that it can be referred
 * to uniquely within the configuration system.
 */
public interface NamedComponent {
    void setName(String name);

    String getName();
}
