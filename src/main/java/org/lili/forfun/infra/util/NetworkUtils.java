package org.lili.forfun.infra.util;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


@Slf4j
public class NetworkUtils {

    /**
     * 获取本机ip
     * @return ip地址
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if ("lo".equals(netInterface.getName())) {
                    // 如果是回环网卡跳过
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        String t = ip.getHostAddress();
                        if (!"127.0.0.1".equals(t)) {
                            // 只返回不是本地的IP
                            return t;
                        }
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            log.error("", e);
            return null;
        }
    }

}
