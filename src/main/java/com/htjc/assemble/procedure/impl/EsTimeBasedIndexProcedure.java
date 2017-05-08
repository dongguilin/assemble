package com.htjc.assemble.procedure.impl;

import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by guilin on 2016/9/28.
 * 计算doc入es的索引
 * 格式: prefix-dateFormat 如 ACCESS-2016-05、logstash-netpay-2016-11
 */
public class EsTimeBasedIndexProcedure extends AbstractProcedure {

    private static final Logger logger = LoggerFactory.getLogger(EsTimeBasedIndexProcedure.class);

    //索引前缀(必填)
    private String prefix;
    //日期格式(非必填，默认为yyyy-MM)
    private String dateformat;

    @Override
    public Doc process(Doc doc) throws Exception {
        String index = new StringBuffer(prefix)
                .append("-")
                .append(new DateTime(doc.getTimestamp()).toString(dateformat)).toString();
        doc.setIndex(index);
        return doc;
    }

    @Override
    public void setConfig(Properties config) throws Exception {
        super.setConfig(config);
        prefix = config.getProperty("prefix");
        if (StringUtils.isBlank(prefix)) {
            throw new IllegalArgumentException("prefix can't be blank");
        }
        dateformat = config.getProperty("dateformat", "yyyy-MM");
        logger.info("{} config:{}", EsTimeBasedIndexProcedure.class.getSimpleName(), config);
    }
}
