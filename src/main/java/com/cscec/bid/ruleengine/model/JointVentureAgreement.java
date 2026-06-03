package com.cscec.bid.ruleengine.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 联营体协议事实对象
 */
@Data
public class JointVentureAgreement implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 是否存在联营体协议 */
    private boolean exists;

    /** 关键条款列表 */
    private List<String> clauses;

    /** 利润分配比例 */
    private String profitDistribution;

    /** 管理权分配 */
    private String managementDistribution;

    /** 争议解决条款 */
    private String disputeResolution;
}
