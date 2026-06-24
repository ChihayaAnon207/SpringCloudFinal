package com.cloudblog.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void init() {
        initGatewayRules();
    }

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 限制 /api/blog/** 路由的总 QPS
        rules.add(new GatewayFlowRule("blog-service")
                .setCount(500)
                .setIntervalSec(1)
        );

        // 限制 /api/user/** 路由
        rules.add(new GatewayFlowRule("user-service")
                .setCount(500)
                .setIntervalSec(1)
        );

        GatewayRuleManager.loadRules(rules);
        log.info("[Sentinel] 网关流控规则已加载: {}", rules);
    }
}
