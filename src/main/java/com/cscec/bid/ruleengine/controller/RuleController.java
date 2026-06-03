package com.cscec.bid.ruleengine.controller;

import com.cscec.bid.ruleengine.model.*;
import com.cscec.bid.ruleengine.service.RuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * 规则引擎 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    // ==================== 规则管理 ====================

    /**
     * 添加或更新单条规则
     * POST /api/rules
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveRule(@Valid @RequestBody RuleDefinition rule) {
        RuleDefinition saved = ruleService.saveRule(rule);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "规则已保存");
        result.put("ruleId", saved.getRuleId());
        return ResponseEntity.ok(result);
    }

    /**
     * 批量添加规则
     * POST /api/rules/batch
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> saveRules(@RequestBody List<RuleDefinition> rules) {
        List<RuleDefinition> saved = ruleService.saveRules(rules);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "批量保存 " + saved.size() + " 条规则");
        result.put("count", saved.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除规则
     * DELETE /api/rules/{ruleId}
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable String ruleId) {
        boolean deleted = ruleService.deleteRule(ruleId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", deleted);
        result.put("message", deleted ? "规则已删除" : "规则不存在");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取单条规则
     * GET /api/rules/{ruleId}
     */
    @GetMapping("/{ruleId}")
    public ResponseEntity<RuleDefinition> getRule(@PathVariable String ruleId) {
        RuleDefinition rule = ruleService.getRule(ruleId);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    /**
     * 列出所有规则
     * GET /api/rules
     */
    @GetMapping
    public ResponseEntity<List<RuleDefinition>> listRules() {
        return ResponseEntity.ok(ruleService.listRules());
    }

    // ==================== 规则执行 ====================

    /**
     * 执行规则判断
     * POST /api/rules/evaluate
     */
    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateResponse> evaluate(@Valid @RequestBody EvaluateRequest request) {
        EvaluateResponse response = ruleService.evaluate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 验证 DRL 语法
     * POST /api/rules/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateDrl(@RequestBody Map<String, String> body) {
        String drl = body.get("drlContent");
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            ruleService.validateDrl(drl);
            result.put("success", true);
            result.put("message", "DRL 语法正确");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    // ==================== 统计 ====================

    /**
     * 获取规则统计
     * GET /api/rules/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(ruleService.getStats());
    }

    /**
     * 健康检查
     * GET /api/rules/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("engine", "Drools");
        result.put("version", "7.74.1.Final");
        result.put("ruleCount", ruleService.getStats().get("totalRules"));
        return ResponseEntity.ok(result);
    }
}
