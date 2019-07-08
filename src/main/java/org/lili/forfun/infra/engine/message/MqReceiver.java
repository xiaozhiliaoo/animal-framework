package org.lili.forfun.infra.engine.message;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.MqConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
class MqReceiver {
    private MqConfig mqConfig;
    private final Address[] addresses;
    private Consumer callback;
    ConnectionFactory factory = new ConnectionFactory();

    public MqReceiver(MqConfig mqConfig, Address[] addresses) {
        log.info("MqReceiver(mqConfig={}, address={}", mqConfig, JSON.toJSONString(addresses));
        this.mqConfig = mqConfig;
        this.addresses = addresses;
        factory.setHost(this.mqConfig.getEndpoints());
        factory.setUsername(this.mqConfig.getUsername());
        factory.setPassword(this.mqConfig.getPassword());
        factory.setPort(this.mqConfig.getPort());
        factory.setAutomaticRecoveryEnabled(true);
    }

    public void setCallback(Consumer callback) {
        this.callback = callback;
    }

    public Channel receiveTopic(String topic) {
        log.info("receiveTopic: {}", topic);
        try {
            Connection conn = factory.newConnection(addresses);
            Channel channel = conn.createChannel();
            String exchangeName = topic;
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT);
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, exchangeName, topic);
            String consumerTag = channel.basicConsume(queueName, true, callback);
            log.info("started topicName: {}, consumerTag: {}", topic, consumerTag);
            return channel;
        } catch (IOException | TimeoutException e) {
            log.error("", e);
            return null;
        }
    }

    public Channel receiveQueue(String queueName) {
        log.info("receiveQueue: {}", queueName);
        try {
            final Connection connection = factory.newConnection(addresses);
            final Channel channel = connection.createChannel();
            channel.basicQos(1);
            String consumerTag = channel.basicConsume(queueName, true, callback);
            log.info("started queueName: {}, consumerTag: {}", queueName, consumerTag);
            return channel;
        } catch (IOException | TimeoutException e) {
            log.error("", e);
            return null;
        }
    }
}