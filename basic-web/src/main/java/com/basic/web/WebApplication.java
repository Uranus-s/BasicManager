package com.basic.web;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com.basic")
@MapperScan("com.basic.dao")
public class WebApplication {

    private static final Logger log = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WebApplication.class, args);
        printDocumentationUrls(context);
    }

    /**
     * 应用完全启动后输出接口文档入口，确保日志中的端口与实际监听端口一致。
     */
    private static void printDocumentationUrls(ConfigurableApplicationContext context) {
        if (!(context instanceof WebServerApplicationContext webContext)) {
            return;
        }

        int port = webContext.getWebServer().getPort();
        String contextPath = context.getEnvironment()
                .getProperty("server.servlet.context-path", "/");
        DocumentationUrls urls = buildDocumentationUrls(port, contextPath);

        log.info("Swagger UI：{}", urls.swaggerUi());
        log.info("OpenAPI JSON：{}", urls.openApiJson());
    }

    /**
     * 统一规范上下文路径，避免根路径或末尾斜杠导致文档地址拼接错误。
     */
    static DocumentationUrls buildDocumentationUrls(int port, String contextPath) {
        String normalizedContextPath = contextPath == null || contextPath.isBlank() || "/".equals(contextPath)
                ? ""
                : "/" + contextPath.replaceAll("^/+|/+$", "");
        String baseUrl = "http://localhost:" + port + normalizedContextPath;
        return new DocumentationUrls(
                baseUrl + "/swagger-ui.html",
                baseUrl + "/v3/api-docs"
        );
    }

    /**
     * 启动日志中展示的接口文档地址。
     */
    record DocumentationUrls(String swaggerUi, String openApiJson) {
    }
}
