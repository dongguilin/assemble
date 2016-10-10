package com.htjc.assemble.test;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by guilin on 2016/10/8.
 */
public class PatternTest {

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
