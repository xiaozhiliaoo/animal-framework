package org.lili.forfun.infra.engine.message;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.MqConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * http://www.rabbitmq.com/documentation.html
 * http://www.rabbitmq.com/java-client.html
 * https://www.rabbitmq.com/getstarted.html
 */
@Slf4j
@Service
@Data
public class MqProcessor {
    @Autowired
    private MqConfig mqConfig;
    private Map<String, Channel> consumerMap = new ConcurrentHashMap<>();
    private MqSender mqSender;
    private MqReceiver mqReceiver;

    @PostConstruct
    public void init() {
        String[] hosts = mqConfig.getEndpoints().split(",");
        Address[] addresses = new Address[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            String host = hosts[i];
            int port = mqConfig.getPort();
            if (host.indexOf(":") > 0) {
                String[] hp = host.split(":");
                host = hp[0];
                port = Integer.parseInt(hp[1]);
            }
            addresses[i] = new Address(host, port);
        }
        mqSender = new MqSender(mqConfig, addresses);
        mqReceiver = new MqReceiver(mqConfig, addresses);
    }

    public String sendMessage(String topic, RabbitmqMessage updateMessage, boolean isBroadCast) {
        String messageId = UUID.randomUUID().toString().replaceAll("-", "");
        if (updateMessage.getUpdateId() == null) {
            updateMessage.setUpdateId(messageId);
        }
        String json = JSON.toJSONString(updateMessage);
        log.info("Send Message: topic:{}, app_id:{}, updateType:{}, groupName:{}, isBroadCast:{}, body: {}", topic,
            updateMessage.getAppKey(),
            updateMessage.getUpdateType(), updateMessage.getGroupKey(), isBroadCast, json);
        if (isBroadCast) {
            mqSender.send2Topic(topic, json);
        } else {
            mqSender.send2Queue(topic, json);
        }
        return messageId;
    }

    public void registerReceiver(final String subscribe, final String topic, MqConsumer callback, boolean isBroadCast) {
        mqReceiver.setCallback(callback);
        Channel c;
        if (isBroadCast) {
            c = mqReceiver.receiveTopic(topic);
        } else {
            c = mqReceiver.receiveQueue(topic);
        }
        if (c == null) {
            log.error("MqProcessor.registerReceiver failed");
        }
        consumerMap.put(subscribe, c);
    }

    public void unRegisterReceiver(final String subscribe) {
        Channel channel = consumerMap.remove(subscribe);
        if (channel != null && channel.isOpen()) {
            closeChannel(channel);
        }
    }

    private void closeChannel(Channel channel) {
        try {
            channel.close();
            channel.getConnection().close();
        } catch (IOException | TimeoutException e) {
            log.error("", e);
        }
    }

    @PreDestroy
    public void destroy() {
        consumerMap.entrySet().forEach(e -> closeChannel(e.getValue()));
    }
}