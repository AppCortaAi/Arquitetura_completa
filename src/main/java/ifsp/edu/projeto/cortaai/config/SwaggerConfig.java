package ifsp.edu.projeto.cortaai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info; // NOVO IMPORT
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement; // NOVO IMPORT
import io.swagger.v3.oas.models.security.SecurityScheme; // NOVO IMPORT
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApiSpec() {
        return new OpenAPI()
                // ADICIONA INFORMAÇÕES BÁSICAS DA API
                .info(new Info()
                        .title("Cortaai API")
                        .description("API para o aplicativo de barbearia Cortaai (Luis Barber Shop)")
                        .version("v1.0"))

                // ADICIONA A DEFINIÇÃO DE SEGURANÇA (BEARER TOKEN)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization"))

                        // MANTÉM SUAS DEFINIÇÕES DE ERRO EXISTENTES
                        .addSchemas("ApiErrorResponse", new ObjectSchema()
                                .addProperty("status", new IntegerSchema())
                                .addProperty("code", new StringSchema())
                                .addProperty("message", new StringSchema())
                                .addProperty("fieldErrors", new ArraySchema().items(
                                        new Schema<ArraySchema>().$ref("ApiFieldError"))))
                        .addSchemas("ApiFieldError", new ObjectSchema()
                                .addProperty("code", new StringSchema())
                                .addProperty("message", new StringSchema())
                                .addProperty("property", new StringSchema())
                                .addProperty("rejectedValue", new ObjectSchema())
                                .addProperty("path", new StringSchema()))
                )

                // ADICIONA O REQUISITO DE SEGURANÇA GLOBAL (O CADEADO)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        // MANTÉM SEU CUSTOMIZER DE ERRO EXISTENTE
        return (operation, handlerMethod) -> {
            operation.getResponses().addApiResponse("4xx/5xx", new ApiResponse()
                    .description("Error")
                    .content(new Content().addMediaType("/", new MediaType().schema(
                            new Schema<MediaType>().$ref("ApiErrorResponse")))));
            return operation;
        };
    }

}