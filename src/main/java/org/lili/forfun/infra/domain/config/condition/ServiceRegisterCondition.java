package org.lili.forfun.infra.domain.config.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class ServiceRegisterCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String global = context.getEnvironment().getProperty("discovery.enabled");
        if (global == null ? false : global.equals("true")) {
            String config = context.getEnvironment().getProperty("discovery.register.enabled");
            return config == null ? false : config.equals("true");
        }
        return false;
    }
}
