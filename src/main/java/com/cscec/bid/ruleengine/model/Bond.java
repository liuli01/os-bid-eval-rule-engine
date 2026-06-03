package com.cscec.bid.ruleengine.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 保函事实对象
 */
@Data
public class Bond implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 保函类型 */
    private String bondType;

    /** 保函金额 */
    private BigDecimal amount;

    /** 到期日 */
    private LocalDate expiryDate;

    /** 开具银行 */
    private String bank;
}
