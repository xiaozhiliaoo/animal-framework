package org.lili.forfun.infra.util;

import com.google.common.base.Charsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ZipUtils {

    public static String md5(String... params) throws NoSuchAlgorithmException {
        if (params == null || params.length == 0) {
            return null;
        } else {
            if (params.length > 1) {
                StringBuilder b = new StringBuilder(params.length);
                for (String param : params) {
                    String msgs = param.trim();
                    if (!msgs.isEmpty()) {
                        b.append(md5(msgs));
                    }
                }
                return md5(b.toString());
            } else {
                return md5(params[0].trim());
            }
        }
    }

    private static String md5(String msgs) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digests = md.digest(msgs.getBytes(Charsets.UTF_8));
        return new String(digests, Charsets.UTF_8);
    }
}
