package com.htjc.assemble.util;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static com.htjc.assemble.util.ConfigConstants.AUXILIARY_FIELD_MARK;

/**
 * Created by guilin on 2016/9/29.
 * 身份证工具类
 * 1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，
 * 八位数字出生日期码，三位数字顺序码和一位数字校验码。
 * 2、地址码(前六位数）表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。
 * 3、出生日期码（第七位至十四位）表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。
 * 4、顺序码（第十五位至十七位）表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 顺序码的奇数分配给男性，偶数分配给女性。
 * 5、校验码（第十八位数）
 */
public class IDCardUtil {

    private static final Logger logger = LoggerFactory.getLogger(IDCardUtil.class);

    //Properties默认编码是iso8859-1
    private static Properties props = new Properties();

    static {
        String fileName = "IDCardAddressCode.properties";
        String userdir = System.getProperty("user.dir");
        File file = new File(userdir + File.separator + "conf" + File.separator + fileName);

        if (!file.exists()) {
            file = new File(userdir + File.separator + "data" + File.separator + fileName);
        }
        InputStream inputStream = null;
        try {
            inputStream = FileUtils.openInputStream(file);
            props.load(inputStream);
        } catch (Exception e) {
            logger.error("load {} error", file.getAbsolutePath(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 根据所传身份证号解析其性别
     *
     * @param cid
     * @return
     */
    public static String parseGender(String cid) {
        String gender = null;
        char c = cid.charAt(cid.length() - 2);
        int sex = Integer.parseInt(String.valueOf(c));
        if (sex % 2 == 0) {
            gender = "女";
        } else {
            gender = "男";
        }
        return gender;
    }

    /**
     * 校验规则：
     * 1、将前面的身份证号码17位数分别乘以不同的系数。第i位对应的数为[2^(18-i)]mod11。从第一位到第十七位的系数分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2 ；
     * 2、将这17位数字和系数相乘的结果相加；
     * 3、用加出来和除以11，看余数是多少？；
     * 4、余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字。其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3 2；
     *
     * @return 返回false说明，身份证号码不符合规则，返回true说明身份证号码符合规则
     */
    public static boolean checkCardId(String cid) {
        boolean flag = false;
        int len = cid.length();
        int kx = 0;
        for (int i = 0; i < len - 1; i++) {
            int x = Integer.parseInt(String.valueOf(cid.charAt(i)));
            int k = 1;
            for (int j = 1; j < 18 - i; j++) {
                k *= 2;
            }
            kx += k * x;
        }
        int mod = kx % 11;
        int[] mods = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        Character[] checkMods = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        for (int i = 0; i < 11; i++) {
            if (mod == mods[i]) {
                Character lastCode = cid.charAt(len - 1);
                if (checkMods[i].equals(lastCode)) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 解析年龄
     *
     * @param cid
     * @return
     */
    public static int parseAge(String cid) {
        int age = 0;
        String birthDayStr = cid.substring(6, 14);
        Date birthDay = null;
        try {
            birthDay = new SimpleDateFormat("yyyyMMdd").parse(birthDayStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthDay)) {
            throw new IllegalArgumentException("您还没有出生么？");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH) + 1;
        int dayBirth = cal.get(Calendar.DAY_OF_MONTH);
        age = yearNow - yearBirth;
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth && dayNow < dayBirth) {
                age--;
            }
        } else {
            age--;
        }
        return age;
    }

    /**
     * 解析出生地址
     *
     * @param cid
     * @return
     */
    public static String parseAddress(String cid) {
        String address = null;
        String addressCode = cid.substring(0, 6);
        try {
            address = new String(props.get(addressCode).toString().getBytes("iso-8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("IDCardAddressCode.properties 文件编码有误", e);
        }
        return address;
    }

    public static Address parse2Address(String cid) throws Exception {
        Address address = new Address();
        try {
            String addressCode = cid.substring(0, 6);
            String addressStr = new String(props.get(addressCode).toString().getBytes("iso-8859-1"), "utf-8");

            if (addressCode.endsWith("0000")) {
                address.setProvince(addressStr);
            } else if (addressCode.endsWith("00")) {
                address.setProvince(new String(props.get(addressCode.substring(0, 2) + "0000").toString().getBytes("iso-8859-1"), "utf-8"));
                address.setCity(addressStr);
            } else {
                address.setProvince(new String(props.get(addressCode.substring(0, 2) + "0000").toString().getBytes("iso-8859-1"), "utf-8"));
                address.setCity(new String(props.get(addressCode.substring(0, 4) + "00").toString().getBytes("iso-8859-1"), "utf-8"));
                address.setRegion(addressStr);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("IDCardAddressCode.properties 文件编码有误", e);
            throw e;
        }
        return address;
    }

    /**
     * 解析出生日期
     *
     * @param cid
     * @return
     */
    public static String parseBirthday(String cid) {
        //通过身份证号来读取出生日期
        String birthday = "";
        //如果没有身份证，那么不进行字符串截取工作。
        if (checkCardId(cid)) {
            String year = cid.substring(6, 10);
            String month = cid.substring(10, 12);
            String day = cid.substring(12, 14);
            birthday = year + "-" + month + "-" + day;
        }
        return birthday;
    }

    public static class Address {
        private String province;//省
        private String city;//市
        private String region;//区

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public Map<String, Object> toMap() {
            if (this == null) return null;
            Map<String, Object> map = new HashedMap();
            map.put(AUXILIARY_FIELD_MARK + "province", this.getProvince());
            if (StringUtils.isNotBlank(this.getCity())) {
                map.put(AUXILIARY_FIELD_MARK + "city", this.getCity());
            }
            if (StringUtils.isNotBlank(this.getRegion())) {
                map.put(AUXILIARY_FIELD_MARK + "region", this.getRegion());
            }
            return map;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "province='" + province + '\'' +
                    ", city='" + city + '\'' +
                    ", region='" + region + '\'' +
                    '}';
        }
    }

}
