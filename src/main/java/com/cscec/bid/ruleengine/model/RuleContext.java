package com.cscec.bid.ruleengine.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 规则执行上下文 - 包含所有传入的事实对象
 */
@Data
public class RuleContext implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 项目信息 */
    private Project project;

    /** 保函信息 */
    private Bond bond;

    /** 联营体协议 */
    private JointVentureAgreement jointVenture;

    /** 其他扩展事实可以在此添加 */
}
