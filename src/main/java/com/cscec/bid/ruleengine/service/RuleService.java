package com.cscec.bid.ruleengine.service;

import com.cscec.bid.ruleengine.engine.DroolsEngine;
import com.cscec.bid.ruleengine.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则业务服务
 */
@Slf4j
@Service
public class RuleService {

    @Autowired
    private DroolsEngine droolsEngine;

    /**
     * 添加/更新规则
     */
    public RuleDefinition saveRule(RuleDefinition rule) {
        rule.setUpdatedAt(LocalDateTime.now());
        if (rule.getCreatedAt() == null) {
            rule.setCreatedAt(LocalDateTime.now());
        }
        droolsEngine.upsertRule(rule.getRuleId(), rule.getDrlContent());
        return rule;
    }

    /**
     * 批量添加规则
     */
    public List<RuleDefinition> saveRules(List<RuleDefinition> rules) {
        Map<String, String> map = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        for (RuleDefinition rule : rules) {
            rule.setUpdatedAt(now);
            if (rule.getCreatedAt() == null) {
                rule.setCreatedAt(now);
            }
            map.put(rule.getRuleId(), rule.getDrlContent());
        }
        droolsEngine.upsertRules(map);
        return rules;
    }

    /**
     * 删除规则
     */
    public boolean deleteRule(String ruleId) {
        return droolsEngine.deleteRule(ruleId);
    }

    /**
     * 获取规则
     */
    public RuleDefinition getRule(String ruleId) {
        String drl = droolsEngine.getRule(ruleId);
        if (drl == null) {
            return null;
        }
        RuleDefinition rule = new RuleDefinition();
        rule.setRuleId(ruleId);
        rule.setDrlContent(drl);
        return rule;
    }

    /**
     * 列出所有规则
     */
    public List<RuleDefinition> listRules() {
        return droolsEngine.listRuleIds().stream()
                .map(id -> {
                    RuleDefinition r = new RuleDefinition();
                    r.setRuleId(id);
                    r.setDrlContent(droolsEngine.getRule(id));
                    return r;
                })
                .collect(Collectors.toList());
    }

    /**
     * 执行规则判断
     */
    public EvaluateResponse evaluate(EvaluateRequest request) {
        long start = System.currentTimeMillis();
        try {
            List<RiskFlag> flags = droolsEngine.evaluate(
                    request.getContext(),
                    request.getAgendaGroup()
            );
            long elapsed = System.currentTimeMillis() - start;
            return EvaluateResponse.ok(flags, elapsed);
        } catch (Exception e) {
            log.error("规则执行失败", e);
            return EvaluateResponse.error(e.getMessage());
        }
    }

    /**
     * 验证 DRL 语法
     */
    public void validateDrl(String drlContent) {
        droolsEngine.validateDrl(drlContent);
    }

    /**
     * 获取规则统计
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRules", droolsEngine.getRuleCount());
        stats.put("ruleIds", droolsEngine.listRuleIds());
        return stats;
    }
}
