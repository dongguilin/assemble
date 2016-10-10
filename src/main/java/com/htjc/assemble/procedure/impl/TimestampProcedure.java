package com.htjc.assemble.procedure.impl;

import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static com.htjc.assemble.util.ConfigConstants.AUXILIARY_FIELD_MARK;

/**
 * Created by guilin on 2016/9/1.
 * 抽取出业务时间
 */
public class TimestampProcedure extends AbstractProcedure {

    private static final Logger logger = LoggerFactory.getLogger(TimestampProcedure.class);

    //字段名称
    private String dateField;

    //字段值的时间格式
    private String dateFormatStr;

    //字段值是否是long型时间戳
    private boolean isTimestamp;

    private DateFormat dateFormat;

    @Override
    public Doc process(Doc doc) {
        Map<String, Object> map = doc.getBody();

        Date date = null;
        if (!map.containsKey(dateField)) {
            date = new Date();
        } else {
            if (!isTimestamp) {
                try {
                    Object obj = map.get(dateField);
                    if (obj == null) return null;
                    date = dateFormat.parse(obj.toString());
                } catch (Exception e) {
                    logger.error("dateField parse error:{}", doc);
                    return null;
                }
            } else {
                date = new Date((long) map.get(dateField));
            }
        }
        map.put(AUXILIARY_FIELD_MARK + "timestamp", date);
        doc.setTimestamp(date.getTime());
        return doc;
    }

    @Override
    public void setConfig(Properties config) {
        super.setConfig(config);
        dateField = config.getProperty("dateField", "CREATE_TIME");
        dateFormatStr = config.getProperty("dateFormatStr", "yyyyMMddHHmmssSSS");
        isTimestamp = Boolean.parseBoolean(config.getProperty("isTimestamp", "false"));
        dateFormat = new SimpleDateFormat(dateFormatStr);
        logger.info("{} config:{}", TimestampProcedure.class.getSimpleName(), config);
    }

}
