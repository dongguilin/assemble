package com.htjc.assemble.procedure.impl;

import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import com.htjc.assemble.util.IDCardUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
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

    //身份证号码字段名称(非必填，默认为idcard)
    private String field;

    @Override
    public Doc process(Doc doc) throws Exception {
        Map<String, Object> map = doc.getBody();

        Object idcardObj = map.get(field);
        if (idcardObj == null) return doc;

        String idcard = idcardObj.toString();
        if (StringUtils.isBlank(idcard)) {
            map.remove(field);//值为空白时，移除该字段
            return doc;
        }

        boolean isValid = IDCardUtil.checkCardId(idcard);
        if (!isValid) {
            throw new IllegalArgumentException("idcard " + idcard + " is not valid");
        }

        //性别(男/女)
        String gender = IDCardUtil.parseGender(idcard);
        map.put(AUXILIARY_FIELD_MARK + "gender", gender);

        //出生日期，如2015-05-12
        String birthday = IDCardUtil.parseBirthday(idcard);
        map.put(AUXILIARY_FIELD_MARK + "birthday", birthday);

        Map<String, Object> addressMap = IDCardUtil.parse2Address(idcard).toMap();
        if (MapUtils.isNotEmpty(addressMap)) {
            map.putAll(addressMap);
        }
        return doc;
    }

    @Override
    public void setConfig(Properties config) throws Exception {
        super.setConfig(config);
        field = config.getProperty("field", "idcard");
        logger.info("{} config:{}", IDCardProcedure.class.getSimpleName(), config);
    }
}
