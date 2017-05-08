package com.htjc.assemble.procedure.impl;

import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import org.apache.commons.lang.StringUtils;
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

    //字段名称(必填)
    private String dateField;

    //字段值的时间格式(isTimestamp=false时，该值必填)
    private String dateFormatStr;

    //字段值是否是long型时间戳，默认为false
    private boolean isTimestamp;

    private DateFormat dateFormat;

    @Override
    public Doc process(Doc doc) throws Exception {
        Map<String, Object> map = doc.getBody();

        Date date = null;
        if (!map.containsKey(dateField)) {
            date = new Date();
        } else {
            if (!isTimestamp) {
                Object obj = map.get(dateField);
                date = dateFormat.parse(obj.toString());
            } else {
                date = new Date((long) map.get(dateField));
            }
        }
        map.put(AUXILIARY_FIELD_MARK + "timestamp", date);
        doc.setTimestamp(date.getTime());
        return doc;
    }

    @Override
    public void setConfig(Properties config) throws Exception {
        super.setConfig(config);
        isTimestamp = Boolean.parseBoolean(config.getProperty("isTimestamp", "false"));
        dateField = config.getProperty("dateField");
        if (StringUtils.isBlank(dateField)) {
            throw new IllegalArgumentException("dateField can't be blank");
        }
        dateFormatStr = config.getProperty("dateFormatStr");
        if (!isTimestamp) {
            if (StringUtils.isBlank(dateFormatStr)) {
                throw new IllegalArgumentException("dateFormatStr can't be blank");
            }
            dateFormat = new SimpleDateFormat(dateFormatStr);
        }

        logger.info("{} config:{}", TimestampProcedure.class.getSimpleName(), config);
    }

}
