package com.htjc.assemble.procedure.impl;

import com.alibaba.fastjson.JSON;
import com.htjc.assemble.model.Doc;
import com.htjc.assemble.procedure.AbstractProcedure;
import com.htjc.assemble.util.IpSearchUtil;
import com.htjc.common.ip.Geography;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.htjc.assemble.util.ConfigConstants.AUXILIARY_FIELD_MARK;

/**
 * Created by guilin on 2016/9/1.
 * 解析ip地址，抽取出洲、国家、省份、城市、运营商、国家代码、经度、纬度等信息
 */
public class GeoIpProcedure extends AbstractProcedure {

    private static final Logger logger = LoggerFactory.getLogger(GeoIpProcedure.class);

    //ip字段名称
    private String ip;

    //经纬度字段名称
    private String geoip;

    @Override
    public Doc process(Doc doc) {
        Map<String, Object> map = doc.getBody();
        if (!map.containsKey(ip)) {
            logger.warn("ip field ({}) is null, doc:{}", ip, JSON.toJSONString(map));
            return doc;
        }

        String ipStr = map.get(ip).toString();
        if (StringUtils.isBlank(ipStr)) {
            logger.warn("ip field ({}) value is blank, doc:{}", ip, ipStr, JSON.toJSONString(map));
            return doc;
        }

        Geography geography = IpSearchUtil.getToVo(ipStr);
        if (geography == null) {
            logger.warn("parse ip={} error, doc:{}", ipStr, JSON.toJSONString(map));
            return doc;
        }

        map.put(AUXILIARY_FIELD_MARK + "continent", geography.getContinent());
        map.put(AUXILIARY_FIELD_MARK + "country", geography.getCountry());
        map.put(AUXILIARY_FIELD_MARK + "province", geography.getProvince());
        map.put(AUXILIARY_FIELD_MARK + "city", geography.getCity());
        map.put(AUXILIARY_FIELD_MARK + "operator", geography.getOperator());
        map.put(geoip, GeoPoint.parseFromLatLon(geography.getLatitude() + "," + geography.getLongitude()));
        return doc;
    }

    @Override
    public void setConfig(Properties config) {
        super.setConfig(config);
        ip = config.getProperty("ipField", "userIP ");
        geoip = config.getProperty("geoipField", "geoip");
        logger.info("{} config:{}", GeoIpProcedure.class.getSimpleName(), config);
    }
}
