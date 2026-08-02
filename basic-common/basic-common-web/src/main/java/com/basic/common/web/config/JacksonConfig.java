package com.basic.common.web.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置。
 */
@Configuration
public class JacksonConfig {

    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public JsonMapperBuilderCustomizer localDateTimeCustomizer() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN);
        SimpleModule module = new SimpleModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        return builder -> builder.addModule(module);
    }
}
