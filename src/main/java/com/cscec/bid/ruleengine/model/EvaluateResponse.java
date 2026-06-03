package com.cscec.bid.ruleengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 规则执行响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    private boolean success;

    /** 触发结果列表 */
    private List<RiskFlag> riskFlags = new ArrayList<>();

    /** 执行耗时(ms) */
    private long elapsedMs;

    /** 消息 */
    private String message;

    public static EvaluateResponse ok(List<RiskFlag> flags, long elapsedMs) {
        return new EvaluateResponse(true, flags, elapsedMs, "执行成功");
    }

    public static EvaluateResponse error(String msg) {
        return new EvaluateResponse(false, new ArrayList<>(), 0, msg);
    }
}
