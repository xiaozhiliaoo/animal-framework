package org.lili.forfun.infra.api;

import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.EtcdConfig;
import org.lili.forfun.infra.domain.config.ServiceConfig;
import org.lili.forfun.infra.domain.constants.ProtoType;
import org.lili.forfun.infra.domain.http.HttpProcessor;
import org.lili.forfun.infra.engine.config.ConfigEngine;
import org.lili.forfun.infra.engine.discovery.DiscoveryEngine;

import java.util.List;
import java.util.Map;
import java.util.Random;


@Slf4j
public class ServiceDiscoveryApi {
    private static final String HTTP_PREFIX = "http://";
    private DiscoveryEngine discoveryEngine;
    private final EtcdConfig etcdConfig;
    private final ConfigEngine configEngine;
    private final HttpProcessor httpProcessor;

    public ServiceDiscoveryApi(ConfigEngine configEngine, HttpProcessor httpProcessor, EtcdConfig etcdConfig) {
        this.etcdConfig = etcdConfig;
        this.configEngine = configEngine;
        this.httpProcessor = httpProcessor;
        this.init();
    }

    public void init() {
        discoveryEngine = new DiscoveryEngine();
        discoveryEngine.init(etcdConfig);
    }

    public void destroy() {

    }

    /**
     * 查询其下的值，转换成["协议,方法","httpUrl"]数组
     *
     * @param serviceKey
     * @return
     */
    public String[] discovery(String serviceKey) {
        try {
            ServiceConfig config = configEngine.getServiceConfig(serviceKey);
            Map<String, String> endpoints = config.getEndpoints();
            String proto = config.getProto();
            String endpoint = endpoints.get(serviceKey);
            if (ProtoType.VIPSERVER.getValue().equals(proto)) {
                // DNS,port,path,[GET/POST/PUT/DELETE]
                String[] eps = endpoint.split(",");
                if (eps == null || eps.length == 0) {
                    return null;
                }
                List<String> ips = discoveryEngine.discovery(serviceKey);
                String ip = randomOne(ips);
                String httpUrl;
                String path = eps[2];
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                String method = eps[3];
                if (ip.indexOf(":") > 0) {
                    httpUrl = HTTP_PREFIX + ip + path;
                } else {
                    int port = Integer.parseInt(eps[1]);
                    if (80 == port) {
                        httpUrl = HTTP_PREFIX + ip + path;
                    } else {
                        httpUrl = HTTP_PREFIX + ip + ":" + port + path;
                    }
                }
                return new String[]{proto + "," + method, httpUrl};
            } else if (ProtoType.HSF.getValue().equals(proto)) {
                return new String[]{ProtoType.HSF.name(), endpoint};
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }


    public String process(String serviceKey, String payload) throws Exception {
        String[] discovery = discovery(serviceKey);
        if (discovery[0].startsWith(ProtoType.VIPSERVER.getValue())) {
            String method = discovery[0].split(",")[1];
            String httpUrl = discovery[1];
            switch (method) {
                case "PUT":
                    return httpProcessor.put(httpUrl, payload);
                case "POST":
                    return httpProcessor.post(httpUrl, payload);
                case "DELETE":
                    return httpProcessor.delete(httpUrl, payload);
                case "GET":
                default:
                    return httpProcessor.get(httpUrl);
            }
        }
        log.error("proto : " + discovery[0] + " is not support");
        return null;
    }

    public ServiceConfig acquireServiceConfig(String appKey) {
        return configEngine.getServiceConfig(appKey);
    }


    Random random = new Random();
    private String lastIp = "";

    private String randomOne(List<String> ips) {
        // 这里是为了避免etcd数据消失的问题
        if (ips != null && ips.size() > 0) {
            int i = random.nextInt(ips.size());
            lastIp = ips.get(i);
        } else {
            log.error("etcd have no config");
        }
        return lastIp;
    }

    /**
     * 返回的值带端口号
     *
     * @param appKey
     * @param serviceKey
     * @return
     */
    public String getHost(String appKey, String serviceKey) {
        List<String> ips = discoveryEngine.discovery(serviceKey);
        return randomOne(ips);
    }

}