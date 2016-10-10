package com.htjc.assemble.procedure.impl;

import com.alibaba.fastjson.JSON;
import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.htjc.assemble.util.ConfigConstants.AUXILIARY_FIELD_MARK;

/**
 * Created by guilin on 2016/10/8.
 * 使用正则拆分字段
 */
public class RegexProcedure extends AbstractProcedure {

    private static final Logger logger = LoggerFactory.getLogger(RegexProcedure.class);

    private List<RegexModel> regexes;

    @Override
    public Doc process(Doc doc) {
        if (CollectionUtils.isEmpty(regexes)) return doc;
        Map<String, Object> map = doc.getBody();

        for (RegexModel model : regexes) {
            String field = model.getField();
            if (!map.containsKey(field) || StringUtils.isBlank(map.get(field).toString())) {
                logger.warn("regex field ({}) is null, doc:{}", field, JSON.toJSONString(map));
            }

            String reg = model.getRegex();
            String value = map.get(field).toString();

            Pattern p = Pattern.compile(reg);
            Matcher m = p.matcher(value);
            if (m.matches()) {
                List<String> fields = model.getFields();
                for (String f : fields) {
                    map.put(AUXILIARY_FIELD_MARK + f, m.group(f));
                }
            }
        }
        return doc;
    }

    @Override
    public void setConfig(Properties config) {
        super.setConfig(config);

        Set<Object> keys = config.keySet();
        if (CollectionUtils.isEmpty(keys)) {
            logger.error("regex fields can't be null");
            return;
        } else {
            logger.info("{} config:{}", RegexProcedure.class.getSimpleName(), config);
        }

        regexes = new ArrayList<>(keys.size());
        for (Object key : keys) {
            String regexStr = config.getProperty(key.toString());
            RegexModel model = new RegexModel(key.toString(), regexStr);
            try {
                Matcher matcher = Pattern.compile("\\?<(.*?)>").matcher(regexStr);
                while (matcher.find()) {
                    model.getFields().add(matcher.group(1));
                }
                regexes.add(model);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private static class RegexModel {
        private String field;//要解析的字段名称
        private String regex;//解析正则表达式
        private List<String> fields;//解析后产生的新字段列表

        public RegexModel(String field, String regex) {
            this.field = field;
            this.regex = regex;
            this.fields = new ArrayList<>();
        }

        public List<String> getFields() {
            return fields;
        }

        public void setFields(List<String> fields) {
            this.fields = fields;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }
    }
}
