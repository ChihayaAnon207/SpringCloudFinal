package com.cloudblog.blog.config;

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

    /**
     * 熔断规则：Feign 调用 user-service 时，如果接口异常比例超过 50%，则熔断
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule();
        // 资源名对应 Feign 客户端的方法：类名:方法签名
        rule.setResource("GET:http://user-service/api/user/{id}");
        // 熔断策略：异常比例 (0=异常数, 1=异常比例, 2=慢调用平均RT)
        rule.setGrade(1);
        // 异常比例阈值：50%
        rule.setCount(0.5);
        // 最小请求数
        rule.setMinRequestAmount(5);
        // 统计时长（秒）
        rule.setStatIntervalMs(10000);
        // 熔断后恢复时长（秒）
        rule.setTimeWindow(30);
        rules.add(rule);

        DegradeRuleManager.loadRules(rules);
        log.info("[Sentinel] 熔断规则已加载: {}", rules);
    }

    /**
     * 流控规则：博客查询接口 QPS 限制
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule pageRule = new FlowRule();
        pageRule.setResource("GET:/api/blog/page");
        pageRule.setGrade(1);  // QPS 维度
        pageRule.setCount(100); // 100 QPS
        pageRule.setControlBehavior(0); // 直接拒绝
        rules.add(pageRule);

        FlowRule likeRule = new FlowRule();
        likeRule.setResource("POST:/api/blog/{id}/like");
        likeRule.setGrade(1);
        likeRule.setCount(50);
        likeRule.setControlBehavior(0);
        rules.add(likeRule);

        FlowRuleManager.loadRules(rules);
        log.info("[Sentinel] 流控规则已加载: {}", rules);
    }
}
