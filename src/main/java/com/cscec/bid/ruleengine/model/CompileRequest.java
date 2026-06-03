package com.cscec.bid.ruleengine.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 规则编译请求（自然语言 -> DRL）
 */
@Data
public class CompileRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    @NotBlank(message = "规则ID不能为空")
    private String ruleId;

    /** 自然语言规则描述 */
    @NotBlank(message = "规则描述不能为空")
    private String ruleText;

    /** 规则类别：P/R */
    private String category;

    /** 规则类型 */
    private String type;

    /** agenda-group 批次 */
    private Integer agendaGroup;

    /** 优先级 */
    private Integer salience;
}
