package com.ruoyi.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.*;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.providers.JavadocProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by xiedong
 * Date: 2024/4/14
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("若依管理")
                        .description("若依管理API文档")
                        .version("v1")
                        .license(new License().name("").url("")))
                .externalDocs(new ExternalDocumentation()
                        .description("")
                        .url(""));
    }


    // ========== 全局 OpenAPI 配置 ==========

//    @Bean
//    public OpenAPI createApi(SwaggerProperties properties) {
//        Map<String, SecurityScheme> securitySchemas = buildSecuritySchemes();
//        OpenAPI openAPI = new OpenAPI()
//                // 接口信息
//                .info(buildInfo(properties))
//                // 接口安全配置
//                .components(new Components().securitySchemes(securitySchemas))
//                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
//        securitySchemas.keySet().forEach(key -> openAPI.addSecurityItem(new SecurityRequirement().addList(key)));
//        return openAPI;
//    }
//
//    /**
//     * API 摘要信息
//     */
//    private Info buildInfo(SwaggerProperties properties) {
//        return new Info()
//                .title(properties.getTitle())
//                .description(properties.getDescription())
//                .version(properties.getVersion())
//                .contact(new Contact().name(properties.getAuthor()).url(properties.getUrl()).email(properties.getEmail()))
//                .license(new License().name(properties.getLicense()).url(properties.getLicenseUrl()));
//    }

    /**
     * 安全模式，这里配置通过请求头 Authorization 传递 token 参数
     */
    private Map<String, SecurityScheme> buildSecuritySchemes() {
        Map<String, SecurityScheme> securitySchemes = new HashMap<>();
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // 类型
                .name(HttpHeaders.AUTHORIZATION) // 请求头的 name
                .in(SecurityScheme.In.HEADER); // token 所在位置
        securitySchemes.put(HttpHeaders.AUTHORIZATION, securityScheme);
        return securitySchemes;
    }

    /**
     * 自定义 OpenAPI 处理器
     */
    @Bean
    public OpenAPIService openApiBuilder(Optional<OpenAPI> openAPI,
                                         SecurityService securityParser,
                                         SpringDocConfigProperties springDocConfigProperties,
                                         PropertyResolverUtils propertyResolverUtils,
                                         Optional<List<OpenApiBuilderCustomizer>> openApiBuilderCustomizers,
                                         Optional<List<ServerBaseUrlCustomizer>> serverBaseUrlCustomizers,
                                         Optional<JavadocProvider> javadocProvider) {

        return new OpenAPIService(openAPI, securityParser, springDocConfigProperties,
                propertyResolverUtils, openApiBuilderCustomizers, serverBaseUrlCustomizers, javadocProvider);
    }

    // ========== 分组 OpenAPI 配置 ==========

    /**
     * 所有模块的 API 分组
     */
    @Bean
    public GroupedOpenApi allGroupedOpenApi() {
        return buildGroupedOpenApi("all", "");
    }

    public static GroupedOpenApi buildGroupedOpenApi(String group) {
        return buildGroupedOpenApi(group, group);
    }

    public static GroupedOpenApi buildGroupedOpenApi(String group, String path) {
        return GroupedOpenApi.builder()
                .group(group)
                .pathsToMatch("/admin-api/" + path + "/**", "/app-api/" + path + "/**", "/**")
                .addOperationCustomizer((operation, handlerMethod) -> operation
                        // .addParametersItem(buildTenantHeaderParameter())
                        .addParametersItem(buildSecurityHeaderParameter()))
                .build();
    }
    public static final String HEADER_TENANT_ID = "tenant-id";
    /**
     * 构建 Authorization 认证请求头参数
     * <p>
     * 解决 Knife4j <a href="https://gitee.com/xiaoym/knife4j/issues/I69QBU">Authorize 未生效，请求header里未包含参数</a>
     *
     * @return 认证参数
     */
    private static Parameter buildSecurityHeaderParameter() {
        return new Parameter()
                .name(HttpHeaders.AUTHORIZATION) // header 名
                .description("认证 Token") // 描述
                .in(String.valueOf(SecurityScheme.In.HEADER)) // 请求 header
                .schema(new StringSchema()._default("Bearer test1").name(HEADER_TENANT_ID).description("认证 Token")); // 默认：使用用户编号为 1
    }

}