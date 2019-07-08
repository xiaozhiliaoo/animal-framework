package org.lili.forfun.infra.engine.discovery;


import org.lili.forfun.infra.domain.constants.Env;
import org.lili.forfun.infra.domain.info.ServiceName;

public interface DiscoveryConstants {
    String SERVICE_PREFIX = "com.idst.";

    /*
     * 服务名配置
     */
    String SERVICE_RAT = ServiceName.RAT.getValue();
    String SERVICE_OX = ServiceName.OX.getValue();
    String SERVICE_TIGER = ServiceName.TIGER.getValue();
    String SERVICE_HARE= ServiceName.HARE.getValue();

    String DEVELOP = "develop";
    String DAILY = Env.DAILY.stringValue();
    String PREPUB = Env.PREPUB.stringValue();
    String PUBLISH = Env.PUBLISH.stringValue();
}