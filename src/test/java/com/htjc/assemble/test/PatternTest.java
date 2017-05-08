package com.htjc.assemble.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by guilin on 2016/10/8.
 */
public class PatternTest {

    @Test
    public void testSimpleMethodName() {
        String methodName = "call(java.lang.String, java.lang.Integer)";
        String regStr = "(?<simpleMethodName>\\w+)(?:(.*))";
        Pattern pattern = Pattern.compile(regStr);

        Matcher matcher = Pattern.compile("\\?<(.*?)>").matcher(regStr);
        assertTrue(matcher.find());
        assertThat(matcher.group(1), is("simpleMethodName"));

        matcher = pattern.matcher(methodName);
        assertTrue(matcher.find());
        assertThat(matcher.group(1), is("call"));
    }

    @Test
    public void testClassFullName() {
        String str = "com.htjc.electric.service.IPowerfeeServices";
        String regStr = "(?:.*[.]{1})(?<invokeName>\\w+)";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(str);
        assertTrue(matcher.find());
        assertThat(matcher.group(1), is("IPowerfeeServices"));

        matcher = Pattern.compile("\\?<(.*?)>").matcher(regStr);
        assertTrue(matcher.find());
        assertThat(matcher.group(1), is("invokeName"));
    }

    @Test
    public void testIDCard() {
        String idstr = "411323198308012652";
        String reg = "(?<addressCode>\\d{6})(?<birthdayCode>\\d{8})(?:.*)";
        Pattern pattern = Pattern.compile(reg);

        List<String> fields = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\?<(.*?)>").matcher(reg);
        while (matcher.find()) {
            fields.add(matcher.group(1));
        }

        matcher = pattern.matcher(idstr);
        if (matcher.matches()) {
            assertThat(fields.get(0), is("addressCode"));
            assertThat(matcher.group(fields.get(0)), is("411323"));
            assertThat(fields.get(1), is("birthdayCode"));
            assertThat(matcher.group(fields.get(1)), is("19830801"));
        }

        reg = "(?<addressCode>(?<city>\\d{2})\\d{6})(?<birthdayCode>\\d{8})(?:.*)";
        pattern = Pattern.compile(reg);
//        matcher = Pattern.compile("\\?<(.*?)>").matcher(reg);
        matcher = pattern.matcher(idstr);
        if (matcher.matches()) {
            System.out.println("a");
        }

    }

    @Test
    public void testIDCard2() {
        String idstr = "411323198308012652";
        String reg = "(?<addressCode>(?<province>\\d{2})\\d{4})(?<birthdayCode>\\d{8})(?:.*)";
        Pattern pattern = Pattern.compile(reg);

        List<String> fields = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\?<(.*?)>").matcher(reg);
        while (matcher.find()) {
            fields.add(matcher.group(1));
        }

        matcher = pattern.matcher(idstr);
        if (matcher.matches()) {
            assertThat(fields.get(0), is("addressCode"));
            assertThat(matcher.group(fields.get(0)), is("411323"));
            assertThat(fields.get(1), is("province"));
            assertThat(matcher.group(fields.get(1)), is("41"));
            assertThat(fields.get(2), is("birthdayCode"));
            assertThat(matcher.group(fields.get(2)), is("19830801"));
        }
    }

    @Test
    public void testMobilePhone() {
        String phone = "18513088534";
        String reg = "(?<mobilePhonePrefix3>\\d{3})(?:\\d{4})(?<mobilePhoneSuffix4>\\d{4})";
        Pattern pattern = Pattern.compile(reg);

        List<String> fields = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\?<(.*?)>").matcher(reg);
        while (matcher.find()) {
            fields.add(matcher.group(1));
        }

        matcher = pattern.matcher(phone);
        if (matcher.matches()) {
            assertThat(fields.get(0), is("mobilePhonePrefix3"));
            assertThat(matcher.group(fields.get(0)), is("185"));
            assertThat(fields.get(1), is("mobilePhoneSuffix4"));
            assertThat(matcher.group(fields.get(1)), is("8534"));
        }
    }

    @Test
    public void testTd() {
        Pattern p = Pattern.compile("(?<=<td>).*(?=</td>)");
        String str = "<td>20分钟</td>";
        Matcher m = p.matcher(str);
        while (m.find()) {
            assertThat(m.group(), is("20分钟"));
        }
    }

}
