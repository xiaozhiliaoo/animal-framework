package org.lili.forfun.infra.engine.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class RabbitmqMessage {
    private String updateId;
    private UpdateType updateType;
    private UpdateEnv updateEnv;
    private String ip;
    private String serviceKey;
    private String groupKey;
    private String appKey;
    /**
     * OK or FAIL
     */
    private String status;
    private String message;
    /**
     * 拓展字段，目前仅NLU服务使用，NLU拓展字段结构见NluOptional
     */
    private String optional;
}
