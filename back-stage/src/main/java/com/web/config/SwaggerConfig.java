package com.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot 中使用 Swagger2 构建 RESTFUL APIs")
                        .version("1.0")
                        .description("API 文档")
                        .contact(new Contact()
                                .name("")
                                .email("")));
    }
}
