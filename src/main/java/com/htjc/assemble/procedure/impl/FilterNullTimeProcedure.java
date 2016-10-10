package com.htjc.assemble.procedure.impl;

import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by guilin on 2016/9/28.
 * 过滤掉以_TIME为后缀且其值为空的字段
 */
public class FilterNullTimeProcedure extends AbstractProcedure {

    @Override
    public Doc process(Doc doc) {
        Map<String, Object> map = doc.getBody();
        Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Object> entry = iter.next();
            String key = entry.getKey();
            if (key.endsWith("_TIME")) {
                Object obj = entry.getValue();
                if (obj == null || StringUtils.isBlank(obj.toString()) || "NULL".equals(obj.toString().toUpperCase())) {
                    iter.remove();
                }
            }
        }
        return doc;
    }
}
