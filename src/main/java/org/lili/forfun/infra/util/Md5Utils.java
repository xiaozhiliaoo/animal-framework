package org.lili.forfun.infra.util;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * md5工具类
 * 
 * @author chouy.zy
 */
@Slf4j
public class Md5Utils {
    /**
     * 计算MD5
     * 
     * @param s
     * @return 如果传入字符串为空，则返回空字符串
     */
    public static byte[] md5(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        String encodeStr = "";

        // string 编码必须为utf-8
        byte[] utfBytes;
        try {
            utfBytes = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            utfBytes = s.getBytes();
        }
        MessageDigest mdTemp;
        try {
            mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(utfBytes);
            return mdTemp.digest();
        } catch (Exception e) {
            throw new Error("Failed to generate MD5 : " + e.getMessage());
        }
    }
}
