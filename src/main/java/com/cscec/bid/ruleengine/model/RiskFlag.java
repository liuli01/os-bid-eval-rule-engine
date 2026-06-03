package com.cscec.bid.ruleengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 规则触发结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskFlag implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private String ruleId;

    /** 触发消息 */
    private String message;

    /** 处置动作：退回补正/人工审查/自动通过 */
    private String action;

    /** 置信度 0.0-1.0 */
    private double confidence;
}
