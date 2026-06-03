# os-bid-eval-rule-engine

> 独立 Drools 规则引擎服务，支持通过 REST API 动态添加/更新/删除规则，内存热加载，零 KJAR 打包。

---

## 项目定位

- **独立部署**：单独 Docker 容器运行，与 RuoYi-Vue / Python 服务通过 REST 交互
- **API 驱动**：规则通过 HTTP API 动态注入，LLM 编译后即时生效
- **轻量**：单容器，无 KIE Server，无 Maven 打包，无 BPMN

---

## 快速开始

### 1. 本地启动

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/os-bid-eval-rule-engine-1.0.0.jar

# 服务启动后访问
http://localhost:8080/api/rules/health
```

### 2. Docker 启动

```bash
# 构建镜像
docker build -t os-bid-eval-rule-engine:1.0.0 .

# 运行
docker run -d -p 8080:8080 --name rule-engine os-bid-eval-rule-engine:1.0.0

# 或 docker-compose
docker-compose up -d
```

---

## API 文档

> **AI 提示**：本项目集成了 Swagger/OpenAPI，服务启动后可通过以下地址查看交互式 API 文档：
> - **Swagger UI**：`http://localhost:8080/swagger-ui.html`
> - **OpenAPI JSON**：`http://localhost:8080/api-docs`
>
> AI 可直接解析 `/api-docs` 获取完整的 API Schema，包括请求/响应模型、字段类型、必填项等元数据。

### 规则管理

#### 添加/更新规则
```http
POST /api/rules
Content-Type: application/json

{
  "ruleId": "P-003",
  "name": "保函金额不足",
  "category": "P",
  "type": "conditional",
  "drlContent": "package com.cscec.bid.rules;\nimport ...",
  "description": "保函金额不足合同额2%",
  "agendaGroup": 2,
  "salience": 200
}
```

#### 批量添加规则
```http
POST /api/rules/batch
Content-Type: application/json

[
  { "ruleId": "P-003", "drlContent": "..." },
  { "ruleId": "R-015", "drlContent": "..." }
]
```

#### 删除规则
```http
DELETE /api/rules/P-003
```

#### 获取规则
```http
GET /api/rules/P-003
```

#### 列出所有规则
```http
GET /api/rules
```

### 规则执行

#### 执行规则判断
```http
POST /api/rules/evaluate
Content-Type: application/json

{
  "context": {
    "project": {
      "id": "P2026-001",
      "name": "雅加达地铁",
      "country": "Indonesia",
      "contractAmount": 56000000,
      "mode": "EPC",
      "profitRate": 0.08,
      "penaltyRate": 0.05
    },
    "bond": {
      "bondType": "履约保函",
      "amount": 1000000
    },
    "jointVenture": {
      "exists": true,
      "clauses": ["单方退出", "利润平分"]
    }
  },
  "agendaGroup": null
}
```

响应：
```json
{
  "success": true,
  "riskFlags": [
    {
      "ruleId": "P-003",
      "message": "保函金额不足合同额2%，退回补正",
      "action": "退回补正",
      "confidence": 0.95
    },
    {
      "ruleId": "R-015",
      "message": "联营体协议存在单方退出条款，需审查对等性",
      "action": "人工审查",
      "confidence": 0.85
    }
  ],
  "elapsedMs": 45,
  "message": "执行成功"
}
```

### 规则验证

#### 验证 DRL 语法
```http
POST /api/rules/validate
Content-Type: application/json

{
  "drlContent": "package test; ..."
}
```

### 统计

```http
GET /api/rules/stats
GET /api/rules/health
```

---

## DRL 规则编写规范

每条规则必须包含：

```drools
package com.cscec.bid.rules;

import com.cscec.bid.ruleengine.model.*;
import java.math.BigDecimal;

global java.util.List riskFlags;

rule "RULE-ID"
    salience 200        // P类=200, R类=100
    agenda-group "batch_XX"  // 1-13
    when
        $p : Project(contractAmount != null)
        $b : Bond(amount != null, amount.compareTo(...) < 0)
    then
        riskFlags.add(new RiskFlag("RULE-ID", "消息", "动作", 置信度));
end
```

### 注意事项

1. **必须声明 `global java.util.List riskFlags;`** —— 引擎会注入结果列表
2. **BigDecimal 比较用 `.compareTo()`** —— 不能用 `> <` 直接比较
3. **所有 Fact 字段判空** —— `contractAmount != null`
4. **agenda-group 命名规范** —— `batch_01` ~ `batch_13`

---

## 架构

```
┌─────────────────┐     HTTP      ┌─────────────────────────────┐
│  RuoYi-Vue      │  ─────────▶   │  os-bid-eval-rule-engine    │
│  / Python 服务   │               │  ┌─────────────────────┐    │
│                 │               │  │ DroolsEngine        │    │
│                 │  ◀─────────   │  │ ┌─────────────────┐ │    │
│                 │   JSON结果     │  │ │ ConcurrentHashMap│ │    │
│                 │               │  │ │ ruleId → DRL    │ │    │
│                 │               │  │ └────────┬────────┘ │    │
│                 │               │  │          ▼          │    │
│                 │               │  │   KieHelper.build() │    │
│                 │               │  │   KieSession        │    │
│                 │               │  └─────────────────────┘    │
└─────────────────┘               └─────────────────────────────┘
```

---

## 与 LLM 编译管道集成

```
自然语言规则（YAML）
    │
    ▼ LLM 编译
DRL 字符串
    │
    ▼ POST /api/rules
规则引擎热加载
    │
    ▼ 秒级生效
```

---

## 参考项目

- `AIVault-Code/ref/fast-drools-spring-boot-starter` —— KieHelper 模式参考
- `AIVault-Code/ref/quarkus-drools-llm` —— LLM + Drools 集成模式

---

## 技术栈

- Java 17
- Spring Boot 2.7.18
- Drools 7.74.1.Final
- KieHelper（内嵌编译，无 KIE Server）
