package com.cscec.bid.ruleengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("os-bid-eval-rule-engine API")
                        .version("1.0.0")
                        .description("Drools 规则引擎独立服务 - 支持 API 动态管理规则、热加载执行")
                        .contact(new Contact()
                                .name("中建海外投标评审系统")
                                .url("https://github.com/cscec/os-bid-eval")));
    }
}
