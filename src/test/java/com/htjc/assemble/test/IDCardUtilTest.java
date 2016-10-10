package com.htjc.assemble.test;

import com.htjc.assemble.util.IDCardUtil;
import org.junit.Test;

/**
 * Created by guilin on 2016/9/29.
 */
public class IDCardUtilTest {

    private static String cid = "411323198308012652";

    @Test
    public void testCheckCardId() {
        boolean flag = IDCardUtil.checkCardId(cid);
        System.out.println("身份证号是否通过校验：\t" + flag);
    }

    @Test
    public void testParseGender() {
        String gender = IDCardUtil.parseGender(cid);
        System.out.println("性别：\t" + gender);
    }

    @Test
    public void testParseAge() {
        int age = IDCardUtil.parseAge(cid);
        System.out.println("年龄：\t" + age);
    }

    @Test
    public void testParseAddress() {
        String address = IDCardUtil.parseAddress(cid);
        System.out.println("出生地：\t" + address);
    }

    @Test
    public void testParseBirthday() {
        String birthday = IDCardUtil.parseBirthday(cid);
        System.out.println("出生日期是：\t" + birthday);
    }
}
