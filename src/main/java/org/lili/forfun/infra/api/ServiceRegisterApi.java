package org.lili.forfun.infra.api;


import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.EtcdConfig;
import org.lili.forfun.infra.engine.discovery.DiscoveryEngine;
import org.lili.forfun.infra.util.NetworkUtils;

import java.util.List;


@Slf4j
public class ServiceRegisterApi {
    private DiscoveryEngine discoveryEngine;
    private EtcdConfig etcdProperties;

    public ServiceRegisterApi(EtcdConfig etcdConfig) {
        this.etcdProperties = etcdConfig;
    }

    public void init() {
        discoveryEngine = new DiscoveryEngine();
        discoveryEngine.init(etcdProperties);
    }

    public void refreshEtcdConfig(EtcdConfig etcdProperties) {
    	this.etcdProperties = etcdProperties;
    	discoveryEngine.init(etcdProperties);
    }

    public void destroy() {

    }

    public String whoAmI(String serviceKey, String sign, String ip) {
        return serviceKey + "_mxgroup";
    }

    public String registerServer(String serviceName,List<Object> optionals, String ip, int port) {
        log.debug("{}", optionals.hashCode());
        if (System.getProperty("server.port") != null) {
            port = Integer.parseInt(System.getProperty("server.port"));
        }
        return discoveryEngine.register(serviceName, ip, port);
    }

    public String registerServer(String serviceName , List<Object> optionals) {
        log.debug("{}", optionals.hashCode());
        int port = 80;
        if (System.getProperty("server.port") != null) {
            port = Integer.parseInt(System.getProperty("server.port"));
        }
        return discoveryEngine.register(serviceName, NetworkUtils.getLocalIp(), port);
    }

    public String unRegisterServer(String prefix, String groupKey) {
        return discoveryEngine.unRegister(prefix, groupKey, NetworkUtils.getLocalIp(), 80);
    }

    public String unRegisterServer(String prefix, String groupKey, String ip, int port) {
        return discoveryEngine.unRegister(prefix, groupKey, ip, port);
    }

    /**
     * 返回随机的一个IP端口
     * 
     * @param serviceName
     * @return
     */
    public String discoveryRandomOne(String serviceName) {
        return discoveryEngine.discoveryRandomOne(serviceName);
    }

    public List<String> discovery(String serviceName) {
        return discoveryEngine.discovery(serviceName);
    }
}
