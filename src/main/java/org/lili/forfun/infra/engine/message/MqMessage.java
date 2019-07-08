package org.lili.forfun.infra.engine.message;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Builder
@Accessors(chain = true)
public class MqMessage {
    private String id;
    private String body;
}
