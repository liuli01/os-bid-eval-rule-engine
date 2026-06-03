package com.cscec.bid.ruleengine.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规则定义
 */
@Data
public class RuleDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 规则ID，如 P-003, R-015 */
    @NotBlank(message = "规则ID不能为空")
    private String ruleId;

    /** 规则名称 */
    private String name;

    /** 规则类别：P类(制度)/R类(经验) */
    private String category;

    /** 规则类型：numeric/conditional/checklist/semantic */
    private String type;

    /** DRL 规则内容 */
    @NotBlank(message = "DRL内容不能为空")
    private String drlContent;

    /** 自然语言描述（原始规则文本） */
    private String description;

    /** agenda-group 批次号 1-13 */
    private Integer agendaGroup;

    /** 优先级 salience */
    private Integer salience;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
