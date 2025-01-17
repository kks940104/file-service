package org.anonymous.global.configs;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
info = @Info(title = "파일 API"
))
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi openApiGroup() {
        return GroupedOpenApi.builder()
                .group("파일 API V1")
                .pathsToMatch("/**") // 문서의 범위
                .build();
    }
}
