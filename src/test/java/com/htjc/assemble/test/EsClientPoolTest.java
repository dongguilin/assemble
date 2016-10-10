package com.htjc.assemble.test;

import com.htjc.assemble.pool.EsClientPool;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.Test;

/**
 * Created by guilin on 2016/9/18.
 */
public class EsClientPoolTest extends BaseTest {

    @Test
    public void test1() throws Exception {
        for (int i = 0; i < 10; i++) {
            Client client = EsClientPool.borrowObject();
            System.out.println(client);
            EsClientPool.returnObject(client);
        }
    }

    @AfterClass
    public static void afterClass() {
        EsClientPool.closePool();
    }


}
