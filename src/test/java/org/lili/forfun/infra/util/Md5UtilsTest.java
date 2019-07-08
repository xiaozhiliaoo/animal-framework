package org.lili.forfun.infra.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lili
 * @description
 * @create 2019/7/8 14:31
 */
public class Md5UtilsTest {

    @Test
    public void md5() {
        byte[] hellos = Md5Utils.md5("hello");
        System.out.println(hellos);
    }
}