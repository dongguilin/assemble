package com.htjc.assemble.test;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guilin on 2016/10/19.
 */
public class MyTest {

    @Test
    public void testQueryMsgFromMq() throws IOException {
        String msgId = "C0A83CDB00002A9F0000000008DFBDC7";

        String str = "E:/app/alibaba-rocketmq/bin/mqadmin.exe " +
                "queryMsgById  -n \"192.168.70.104:9876;192.168.70.111:9876\" -i " + msgId;
        Process process = Runtime.getRuntime().exec(str);
        List<String> list = IOUtils.readLines(process.getInputStream());
        for (String s : list) {
            System.out.println(s);
        }
    }


    @Test
    public void testPrintStackTrace() {
        StringWriter writer = new StringWriter();
        try {
            new ArrayList<>().get(0);
        } catch (Throwable e) {
            e.printStackTrace(new PrintWriter(writer));
            System.out.println(writer.toString());
        }
    }
}
