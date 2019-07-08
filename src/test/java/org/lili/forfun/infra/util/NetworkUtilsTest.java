package org.lili.forfun.infra.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lili
 * @description
 * @create 2019/7/8 14:29
 */
public class NetworkUtilsTest {

    @Test
    public void getLocalIp() {
        System.out.println(NetworkUtils.getLocalIp());
    }
}