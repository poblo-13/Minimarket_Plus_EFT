package com.minimarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minimarket API")
                        .version("v1")
                        .description("API REST para la gestión de Minimarket."))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Token JWT obtenido desde POST /auth/login"
                                                )
                                )
                );
    }

    /**
     * Keeps the generated document honest for cross-cutting RFC 9457 failures.
     * Controller annotations remain the source of each operation's success body;
     * this fills the common error contract consistently for legacy mappings too.
     */
    @Bean
    public OpenApiCustomizer problemAndSecurityContract() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            if (path.startsWith("/api/")) {
                operation.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"));
            }
            for (String code : new String[]{"400", "401", "403", "404", "409", "500"}) {
                ApiResponse response = operation.getResponses().get(code);
                if (response == null) {
                    operation.getResponses().addApiResponse(code, problemResponse());
                } else if (response.getContent() == null || !response.getContent().containsKey(PROBLEM_JSON)) {
                    response.setContent(problemContent());
                }
            }
        }));
    }

    private ApiResponse problemResponse() {
        return new ApiResponse().description("Error RFC 9457").content(problemContent());
    }

    private Content problemContent() {
        return new Content().addMediaType(PROBLEM_JSON,
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail")));
    }
}
