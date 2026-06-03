package com.cscec.bid.ruleengine.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 规则执行请求
 */
@Data
public class EvaluateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 执行上下文 */
    @NotNull(message = "执行上下文不能为空")
    private RuleContext context;

    /** 指定只执行的 agenda-group（可选，不传则执行全部） */
    private Integer agendaGroup;
}
