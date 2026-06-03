package com.cscec.bid.ruleengine.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 项目事实对象
 */
@Data
public class Project implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private String id;

    /** 项目名称 */
    private String name;

    /** 国别 */
    private String country;

    /** 合同额（美元） */
    private BigDecimal contractAmount;

    /** 项目模式：EPC/ECI/DB 等 */
    private String mode;

    /** 风险等级 */
    private String riskLevel;

    /** 利润率 */
    private BigDecimal profitRate;

    /** 工期罚款比例 */
    private BigDecimal penaltyRate;

    /** 业主资信 */
    private String ownerCredit;

    /** 月进度付款比例 */
    private BigDecimal monthlyPaymentRate;
}
