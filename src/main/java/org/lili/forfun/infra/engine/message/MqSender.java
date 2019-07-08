package org.lili.forfun.infra.engine.message;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.MqConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


@Slf4j
class MqSender {
    private MqConfig mqConfig;
    private Address[] addresses;
    private ConnectionFactory factory = new ConnectionFactory();

    public MqSender(MqConfig mqConfig, Address[] addresses) {
        this.mqConfig = mqConfig;
        this.addresses = addresses;
        factory.setHost(this.mqConfig.getEndpoints());
        factory.setUsername(this.mqConfig.getUsername());
        factory.setPassword(this.mqConfig.getPassword());
        factory.setPort(this.mqConfig.getPort());
    }

    public void send2Topic(String topic, String message) {
        byte[] messageBodyBytes = message.getBytes();
        try (Connection conn = factory.newConnection(addresses); Channel channel = conn.createChannel()) {
            // channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            channel.exchangeDeclare(topic, BuiltinExchangeType.FANOUT);
            channel.basicPublish(topic, topic, null, messageBodyBytes);
            log.info("Sent to topic[{}] message[{}]", topic, message);
        } catch (IOException | TimeoutException e) {
            log.error("", e);
        }
    }

    public void send2Queue(String queue, String message) {
        try (Connection conn = factory.newConnection(addresses); Channel channel = conn.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            channel.basicPublish("", queue,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes("UTF-8"));
            log.info("Sent to queue[{}] message[{}]", queue, message);
        } catch (IOException | TimeoutException e) {
            log.error("", e);
        }
    }
}
