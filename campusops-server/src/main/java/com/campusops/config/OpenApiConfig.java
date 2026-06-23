package com.campusops.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档基础信息配置。具体路径在 application.yml 的 springdoc 节点维护，
 * 配合 server.servlet.context-path 形成最终访问地址 /campusops/swagger-ui.html。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusOpsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CampusOps API")
                        .description("面向校园 IT 服务的 AI 增强型工单管理平台后端接口文档")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact().name("CampusOps")));
    }
}
