package com.htjc.assemble.procedure.impl;

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

    //ip字段名称(非必填，默认为userIP)
    private String ip;

    //经纬度字段名称(非必填，默认为geoip)
    private String geoip;

    @Override
    public Doc process(Doc doc) throws Exception {
        Map<String, Object> map = doc.getBody();
        Object obj = map.get(ip);
        if (obj == null) return doc;

        String ipStr = obj.toString();
        if (StringUtils.isBlank(ipStr)) {
            map.remove(ip);//值为空白时，移除该字段
            return doc;
        }

        //TODO 需要测试
        Geography geography = IpSearchUtil.getToVo(ipStr);
        if (geography != null) {
            map.put(AUXILIARY_FIELD_MARK + "continent", geography.getContinent());
            map.put(AUXILIARY_FIELD_MARK + "country", geography.getCountry());
            map.put(AUXILIARY_FIELD_MARK + "province", geography.getProvince());
            map.put(AUXILIARY_FIELD_MARK + "city", geography.getCity());
            map.put(AUXILIARY_FIELD_MARK + "operator", geography.getOperator());
            map.put(geoip, GeoPoint.parseFromLatLon(geography.getLatitude() + "," + geography.getLongitude()));
        }
        return doc;
    }

    @Override
    public void setConfig(Properties config) throws Exception {
        super.setConfig(config);
        ip = config.getProperty("ipField", "userIP ");
        geoip = config.getProperty("geoipField", "geoip");
        logger.info("{} config:{}", GeoIpProcedure.class.getSimpleName(), config);
    }
}
