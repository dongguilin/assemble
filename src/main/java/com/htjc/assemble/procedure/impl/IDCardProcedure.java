package com.htjc.assemble.procedure.impl;

import com.alibaba.fastjson.JSON;
import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import com.htjc.assemble.util.IDCardUtil;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.htjc.assemble.util.ConfigConstants.AUXILIARY_FIELD_MARK;

/**
 * Created by guilin on 2016/9/29.
 * 解析身份证号码获取出生地址、性别、出生年月等信息
 */
public class IDCardProcedure extends AbstractProcedure {

    private static final Logger logger = LoggerFactory.getLogger(IDCardProcedure.class);

    //身份证号码字段名称
    private String field;

    @Override
    public Doc process(Doc doc) {
        Map<String, Object> map = doc.getBody();
        if (!map.containsKey(field)) {
            logger.warn("idcard field ({}) is null, doc:{}", field, JSON.toJSONString(map));
            return doc;
        }

        String idcard = map.get(field).toString();
        boolean isValid = IDCardUtil.checkCardId(idcard);
        if (!isValid) {
            logger.error("idcard field = {} is valid, doc:{}", idcard, JSON.toJSONString(map));
            return doc;
        }
        String gender = IDCardUtil.parseGender(idcard);
        String birthday = IDCardUtil.parseBirthday(idcard);
        Map<String, Object> addressMap = IDCardUtil.parse2Address(idcard).toMap();
        if (MapUtils.isNotEmpty(addressMap)) {
            map.putAll(addressMap);
        }
        map.put(AUXILIARY_FIELD_MARK + "gender", gender);
        map.put(AUXILIARY_FIELD_MARK + "birthday", birthday);
        return doc;
    }

    @Override
    public void setConfig(Properties config) {
        super.setConfig(config);
        field = config.getProperty("field", "idcard");
        logger.info("{} config:{}", IDCardProcedure.class.getSimpleName(), config);
    }
}
