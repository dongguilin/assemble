package com.htjc.assemble.procedure.impl;

import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by guilin on 2016/9/28.
 * 计算doc入es的索引
 * 格式: indexPrefix-dateFormat
 */
public class EsTimeBasedIndexProcedure extends AbstractProcedure {

    private static final Logger logger = LoggerFactory.getLogger(EsTimeBasedIndexProcedure.class);

    //索引前缀
    private String prefix;
    //日期格式
    private String dateformat;

    @Override
    public Doc process(Doc doc) {
        String index = new StringBuilder(prefix).append("-").append(new DateTime(doc.getTimestamp()).toString(dateformat)).toString();
        doc.setIndex(index);
        return doc;
    }

    @Override
    public void setConfig(Properties config) {
        super.setConfig(config);
        prefix = config.getProperty("prefix");
        dateformat = config.getProperty("dateformat", "yyyy-MM");
        logger.info("{} config:{}", EsTimeBasedIndexProcedure.class.getSimpleName(), config);
    }
}
