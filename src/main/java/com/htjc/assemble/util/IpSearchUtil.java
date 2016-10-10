package com.htjc.assemble.util;

import com.htjc.common.ip.Geography;
import com.htjc.common.ip.IpSearch;

import java.io.File;

/**
 * Created by guilin on 2016/9/28.
 */
public class IpSearchUtil {

    private static IpSearch ipSearch;

    /**
     * 多线程情况下启用多份IpSearch实例，就会加载多次*.dat数据文件，可能占用过多内存。
     * 目前暂时改为仅加载一次，单线程调用ip解析
     * 要解析的ip过多，单线程解析过慢时，可废弃该工具类
     *
     * @param ip
     * @return
     */
    public static synchronized Geography getToVo(String ip) {
        if (ipSearch == null) {
            String path = "data/ip-utf82.dat";
            if (new File(path).exists()) {
                ipSearch = IpSearch.getInstance(path);
            } else {
                ipSearch = IpSearch.getInstance("conf/ip-utf82.dat");
            }
        }
        return ipSearch.getToVo(ip);
    }

}
