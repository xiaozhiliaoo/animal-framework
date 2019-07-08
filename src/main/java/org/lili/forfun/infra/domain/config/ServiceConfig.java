package org.lili.forfun.infra.domain.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServiceConfig {
    /**
     * 服务名
     */
    private String serviceKey;

    /**
     * 服务协议类型，sm/hsf/vip
     */
    private String proto;

    /**
     * 服务协议信息
     * VIP Server: DNS,port,path,[GET/POST/PUT/DELETE]
     * HSF: interfaceName,methodName,groupVersion
     */
    private Map<String, String> endpoints;
    /**
     * 消息队列配置
     */
    private String mqSendTopic;
    private String mqReceiveTopic;
    private String mqReceiveSubscribe;

    /**
     * 是否启用自动迁移
     */
    private boolean autoScale = false;
    /**
     * 冷热
     */
    private int cpuThreshold;
    private int appQpsThreshold;
    /**
     * 疏密
     */
    private int memThreshold;
    private int appCountThreshold;

    // 扩缩相关配置，包括astro和psp
    /**
     * astro 签名
     */
    private String astroSign;
    /**
     * astro pe 为邮箱前缀
     */
    private String astroOwner;
    /**
     * psp 签名
     */
    private String pspSign;
    /**
     * psp 提交者，为工号
     */
    private String pspSubmitter;

    /**
     * 服务的分组列表
     */
    private List<String> groupKeys;

    public List<String> getGroupKeys() {
        if (this.groupKeys == null) {
            this.groupKeys = new ArrayList<>();
        }
        return this.groupKeys;
    }
}