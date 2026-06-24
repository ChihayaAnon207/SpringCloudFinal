package com.cloudblog.comment.config;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class SentinelRuleConfig {

    @PostConstruct
    public void init() {
        initDegradeRules();
        initFlowRules();
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        rule.setResource("GET:http://user-service/api/user/{id}");
        rule.setGrade(1);
        rule.setCount(0.5);
        rule.setMinRequestAmount(5);
        rule.setStatIntervalMs(10000);
        rule.setTimeWindow(30);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
        log.info("[Sentinel] 熔断规则已加载: {}", rules);
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("GET:/api/comment/blog/{blogId}");
        rule.setGrade(1);
        rule.setCount(200);
        rule.setControlBehavior(0);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
        log.info("[Sentinel] 流控规则已加载: {}", rules);
    }
}
