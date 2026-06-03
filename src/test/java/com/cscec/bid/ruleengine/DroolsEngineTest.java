package com.cscec.bid.ruleengine;

import com.cscec.bid.ruleengine.engine.DroolsEngine;
import com.cscec.bid.ruleengine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DroolsEngineTest {

    @Resource
    private DroolsEngine droolsEngine;

    @BeforeEach
    void setUp() {
        droolsEngine.clear();
    }

    @Test
    void testAddRuleAndEvaluate() {
        // 添加一条测试规则
        String drl = "package com.cscec.bid.rules;\n" +
                "import com.cscec.bid.ruleengine.model.*;\n" +
                "global java.util.List riskFlags;\n" +
                "rule \"TEST-001\"\n" +
                "    salience 100\n" +
                "    agenda-group \"batch_01\"\n" +
                "    when\n" +
                "        $p : Project(country == \"Indonesia\")\n" +
                "    then\n" +
                "        riskFlags.add(new RiskFlag(\"TEST-001\", \"印尼项目触发\", \"人工审查\", 0.9));\n" +
                "end";

        droolsEngine.upsertRule("TEST-001", drl);
        assertEquals(1, droolsEngine.getRuleCount());

        // 构建上下文
        Project project = new Project();
        project.setCountry("Indonesia");

        RuleContext ctx = new RuleContext();
        ctx.setProject(project);

        // 执行
        List<RiskFlag> flags = droolsEngine.evaluate(ctx, null);

        // 验证
        assertEquals(1, flags.size());
        assertEquals("TEST-001", flags.get(0).getRuleId());
        assertEquals("印尼项目触发", flags.get(0).getMessage());
    }

    @Test
    void testNoTrigger() {
        String drl = "package com.cscec.bid.rules;\n" +
                "import com.cscec.bid.ruleengine.model.*;\n" +
                "global java.util.List riskFlags;\n" +
                "rule \"TEST-002\"\n" +
                "    agenda-group \"batch_01\"\n" +
                "    when\n" +
                "        $p : Project(country == \"Indonesia\")\n" +
                "    then\n" +
                "        riskFlags.add(new RiskFlag(\"TEST-002\", \"触发\", \"自动\", 1.0));\n" +
                "end";

        droolsEngine.upsertRule("TEST-002", drl);

        Project project = new Project();
        project.setCountry("Singapore");  // 不匹配

        RuleContext ctx = new RuleContext();
        ctx.setProject(project);

        List<RiskFlag> flags = droolsEngine.evaluate(ctx, null);
        assertTrue(flags.isEmpty());
    }

    @Test
    void testAgendaGroup() {
        String drl = "package com.cscec.bid.rules;\n" +
                "import com.cscec.bid.ruleengine.model.*;\n" +
                "global java.util.List riskFlags;\n" +
                "rule \"TEST-B1\"\n" +
                "    agenda-group \"batch_01\"\n" +
                "    when\n" +
                "        Project()\n" +
                "    then\n" +
                "        riskFlags.add(new RiskFlag(\"TEST-B1\", \"批次1\", \"自动\", 1.0));\n" +
                "end\n" +
                "rule \"TEST-B2\"\n" +
                "    agenda-group \"batch_02\"\n" +
                "    when\n" +
                "        Project()\n" +
                "    then\n" +
                "        riskFlags.add(new RiskFlag(\"TEST-B2\", \"批次2\", \"自动\", 1.0));\n" +
                "end";

        droolsEngine.upsertRule("TEST-AG", drl);

        RuleContext ctx = new RuleContext();
        ctx.setProject(new Project());

        // 只执行 batch_01
        List<RiskFlag> flags = droolsEngine.evaluate(ctx, 1);
        assertEquals(1, flags.size());
        assertEquals("TEST-B1", flags.get(0).getRuleId());
    }

    @Test
    void testDeleteRule() {
        droolsEngine.upsertRule("R1", "package test; global java.util.List riskFlags; rule \"R1\" when then end");
        assertTrue(droolsEngine.deleteRule("R1"));
        assertFalse(droolsEngine.deleteRule("NOT-EXIST"));
        assertEquals(0, droolsEngine.getRuleCount());
    }

    @Test
    void testBondAmountRule() {
        // P-003 保函金额不足规则
        String drl = "package com.cscec.bid.rules;\n" +
                "import com.cscec.bid.ruleengine.model.*;\n" +
                "import java.math.BigDecimal;\n" +
                "global java.util.List riskFlags;\n" +
                "rule \"P-003\"\n" +
                "    salience 200\n" +
                "    agenda-group \"batch_02\"\n" +
                "    when\n" +
                "        $p : Project(contractAmount != null)\n" +
                "        $b : Bond(amount != null, amount.compareTo($p.contractAmount.multiply(new BigDecimal(\"0.02\"))) < 0)\n" +
                "    then\n" +
                "        riskFlags.add(new RiskFlag(\"P-003\", \"保函不足\", \"退回补正\", 0.95));\n" +
                "end";

        droolsEngine.upsertRule("P-003", drl);

        Project project = new Project();
        project.setContractAmount(new BigDecimal("100000000")); // 1亿

        Bond bond = new Bond();
        bond.setAmount(new BigDecimal("1000000")); // 100万 < 2%

        RuleContext ctx = new RuleContext();
        ctx.setProject(project);
        ctx.setBond(bond);

        List<RiskFlag> flags = droolsEngine.evaluate(ctx, null);
        assertEquals(1, flags.size());
        assertEquals("P-003", flags.get(0).getRuleId());
    }
}
