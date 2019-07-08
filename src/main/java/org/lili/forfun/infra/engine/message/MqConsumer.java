package org.lili.forfun.infra.engine.message;

import com.google.common.base.Charsets;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class MqConsumer implements Consumer {
    @Override
    public void handleConsumeOk(String consumerTag) {
        log.info("handleConsumeOk " + consumerTag);
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        log.info("handleCancelOk " + consumerTag);
    }

    @Override
    public void handleCancel(String consumerTag) {
        log.info("handleCancel " + consumerTag);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        log.info("handleShutdownSignal " + consumerTag);
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        log.info("handleRecoverOk " + consumerTag);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
        String message = new String(body, Charsets.UTF_8);
        log.debug(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
        consumeMessage(MqMessage.builder().id(consumerTag).body(message).build());
    }

    public abstract void consumeMessage(MqMessage message);
}
