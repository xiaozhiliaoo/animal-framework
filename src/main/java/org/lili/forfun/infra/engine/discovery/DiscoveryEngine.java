package org.lili.forfun.infra.engine.discovery;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.Lease.KeepAliveListener;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.lease.LeaseKeepAliveResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.EtcdConfig;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * https://github.com/coreos/jetcd
 */
@Slf4j
public class DiscoveryEngine {

    public static final int TTL = 5;

    private Map<String, DiscoveryKeepAlive> leaseMap = new ConcurrentHashMap<>(1);
    private String endpoints;
    private Random random = new Random();
    private Client etcdClient;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 可以多次调用来更新配置
     *
     * @param etcdConfig
     */
    public void init(EtcdConfig etcdConfig) {
        // 只有配置有变化才新建连接池
        if (endpoints != null && endpoints.equals(etcdConfig.getEndpoints())) {
            return;
        } else {
            log.info("init(etcdConfig: {})", etcdConfig);
            endpoints = etcdConfig.getEndpoints();
            try {
                lock.writeLock().lock();
                if (etcdClient != null) {
                    this.etcdClient.close();
                }
                this.etcdClient = Client.builder().endpoints(endpoints.split(",")).build();
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    public void refreshConfig(EtcdConfig etcdProperties) {
        init(etcdProperties);
    }



    public String register(String serviceName,String nodeIp, int port) {
        log.info("register(svcName={}, nodeip={}, port={})", serviceName, nodeIp, port);
        try {
            String leaseKey = serviceName + "/" + nodeIp + "/" + port;
            ByteSequence k = ByteSequence.fromString(leaseKey);
            ByteSequence v = ByteSequence.fromString(nodeIp + ":" + port);
            Lease lease = getLease();
            long leaseID = lease.grant(TTL).get().getID();
            log.info("leaseID = {}", leaseID);
            PutOption option = PutOption.newBuilder().withLeaseId(leaseID).build();
            KV kvClient = getKv();
            PutResponse putResponse = kvClient.put(k, v, option).get();
            log.info("Register: serviceName={},nodeIp={},port={},revision={}",
                    serviceName, nodeIp, port, putResponse.getHeader().getRevision());

            KeepAliveListener keepAliveListener = lease.keepAlive(leaseID);
            LeaseKeepAliveResponse keepAliveResponse = keepAliveListener.listen();
            long id = keepAliveResponse.getID();

            leaseMap.put(leaseKey, DiscoveryKeepAlive.builder()
                    .keepAliveListener(keepAliveListener)
                    .keepAliveId(keepAliveResponse.getID())
                    .ttl(keepAliveResponse.getTTL())
                    .build());
            return String.valueOf(id);
        } catch (InterruptedException | ExecutionException e) {
            log.error("", e);
            return null;
        }
    }

    public String unRegister(String serviceName, String groupKey, String nodeIp, int port) {
        String leaseKey = serviceName + "/" + groupKey + "/" + nodeIp + "/" + port;
        DiscoveryKeepAlive keepAlive = leaseMap.remove(leaseKey);
        if (keepAlive != null) {
            log.info("unRegister:keepAliveId={}, ttl={}", keepAlive.getKeepAliveId(), keepAlive.getTtl());
            keepAlive.getKeepAliveListener().close();
            return String.valueOf(keepAlive.getKeepAliveId());
        }
        return "-1";
    }

    /**
     * 返回随机的一个IP端口
     *
     * @param serviceName
     * @return
     */
    public String discoveryRandomOne(String serviceName) {
        List<String> l = discovery(serviceName);
        if (l == null || l.size() == 0) {
            return null;
        } else {
            return l.get(random.nextInt(l.size()));
        }
    }

    public List<String> discovery(String serviceName) {
        log.info("discovery(svcName={})", serviceName);
        String key = serviceName + "/" ;
        try {
            lock.readLock().lock();
            KV kvClient = getKv();
            ByteSequence prefix = ByteSequence.fromString(key);
            GetResponse getResponse = kvClient.get(
                    prefix,
                    GetOption.newBuilder().withPrefix(prefix).build()).get();
            List<KeyValue> kvs = getResponse.getKvs();
            if (kvs == null || kvs.isEmpty()) {
                return null;
            }
            // kvClient.delete(prefix);
            List<String> l = kvs.parallelStream().map(kv -> kv.getValue().toStringUtf8()).collect(Collectors.toList());
            log.info("key: {}, list: {}", key, l);
            return l;
        } catch (InterruptedException | ExecutionException e) {
            log.error("", e);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public KV getKv() {
        return this.etcdClient.getKVClient();
    }

    public Lease getLease() {
        return this.etcdClient.getLeaseClient();
    }
}