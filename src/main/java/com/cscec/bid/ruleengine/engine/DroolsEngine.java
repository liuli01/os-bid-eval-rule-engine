package com.cscec.bid.ruleengine.engine;

import com.cscec.bid.ruleengine.model.*;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.builder.Message;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Drools 规则引擎核心
 * 支持 API 动态添加/更新/删除规则，内存热加载
 */
@Slf4j
@Component
public class DroolsEngine {

    /** 规则缓存：ruleId -> DRL 内容 */
    private final Map<String, String> drlCache = new ConcurrentHashMap<>();

    /**
     * 添加或更新规则
     */
    public void upsertRule(String ruleId, String drlContent) {
        validateDrl(drlContent);
        drlCache.put(ruleId, drlContent);
        log.info("规则已更新: {}", ruleId);
    }

    /**
     * 批量更新规则
     */
    public void upsertRules(Map<String, String> rules) {
        rules.forEach((id, drl) -> {
            validateDrl(drl);
            drlCache.put(id, drl);
        });
        log.info("批量更新 {} 条规则", rules.size());
    }

    /**
     * 删除规则
     */
    public boolean deleteRule(String ruleId) {
        return drlCache.remove(ruleId) != null;
    }

    /**
     * 获取规则内容
     */
    public String getRule(String ruleId) {
        return drlCache.get(ruleId);
    }

    /**
     * 列出所有规则ID
     */
    public Set<String> listRuleIds() {
        return new HashSet<>(drlCache.keySet());
    }

    /**
     * 清空所有规则
     */
    public void clear() {
        drlCache.clear();
    }

    /**
     * 获取规则数量
     */
    public int getRuleCount() {
        return drlCache.size();
    }

    /**
     * 执行规则判断
     *
     * @param context    规则上下文（包含所有事实对象）
     * @param agendaGroup 指定 agenda-group（可选，null 则执行全部）
     * @return 触发结果列表
     */
    public List<RiskFlag> evaluate(RuleContext context, Integer agendaGroup) {
        if (drlCache.isEmpty()) {
            log.warn("规则缓存为空，无法执行");
            return Collections.emptyList();
        }

        // 拼接所有 DRL
        String allDrl = drlCache.values().stream()
                .collect(Collectors.joining("\n\n"));

        // KieHelper 编译（核心）
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(allDrl, ResourceType.DRL);
        var results = kieHelper.verify();
        if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
            var errors = results.getMessages(Message.Level.WARNING, Message.Level.ERROR);
            for (Message msg : errors) {
                log.error("DRL 编译错误: {}", msg.getText());
            }
            throw new IllegalStateException("DRL 编译失败，请检查规则语法");
        }

        KieSession session = kieHelper.build().newKieSession();
        List<RiskFlag> resultsList = new ArrayList<>();
        session.setGlobal("riskFlags", resultsList);

        // 注入事实对象
        if (context.getProject() != null) {
            session.insert(context.getProject());
        }
        if (context.getBond() != null) {
            session.insert(context.getBond());
        }
        if (context.getJointVenture() != null) {
            session.insert(context.getJointVenture());
        }

        // 执行规则
        if (agendaGroup != null) {
            // 执行指定批次（统一两位格式）
            String groupName = String.format("batch_%02d", agendaGroup);
            session.getAgenda().getAgendaGroup(groupName).setFocus();
            session.fireAllRules();
        } else {
            // 执行全部 13 批次：逐个 setFocus + fireAllRules
            for (int i = 1; i <= 13; i++) {
                String groupName = String.format("batch_%02d", i);
                session.getAgenda().getAgendaGroup(groupName).setFocus();
                session.fireAllRules();
            }
        }

        session.dispose();
        return resultsList;
    }

    /**
     * 验证 DRL 语法
     */
    public void validateDrl(String drlContent) {
        try {
            KieHelper helper = new KieHelper();
            helper.addContent(drlContent, ResourceType.DRL);
            var results = helper.verify();
            if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
                var errors = results.getMessages(Message.Level.WARNING, Message.Level.ERROR);
                String errorText = errors.stream()
                        .map(Message::getText)
                        .collect(Collectors.joining("; "));
                throw new IllegalArgumentException("DRL 语法错误: " + errorText);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("DRL 验证失败: " + e.getMessage(), e);
        }
    }
}
