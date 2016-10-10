package com.htjc.assemble.procedure;

import com.htjc.assemble.util.ConfigUtil;

/**
 * Created by guilin on 2016/9/1.
 * 工序实例化工厂
 */
public class ProcedureFactory {

    /**
     * Instantiate specified class, either alias or fully-qualified class name.
     */
    public static Procedure newInstance(String name) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<? extends Procedure> clazz = lookup(name);
        if (clazz == null) {
            clazz = (Class<? extends Procedure>) Class.forName(name);
        }
        return clazz.newInstance();
    }

    private static Class<? extends Procedure> lookup(String name) {
        return ConfigUtil.getProcedures().get(name);
    }

}
