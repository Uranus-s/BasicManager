package com.basic.sericve.sysFile.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件上传配置。
 * <p>
 * 通过 storageType 选择具体存储策略，当前默认 local，后续可扩展 oss、minio 等实现。
 * </p>
 *
 * @author Gas
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "basic.file")
public class FileStorageProperties {

    /**
     * 当前使用的存储类型。
     */
    private String storageType = "local";

    /**
     * 本地存储配置。
     */
    private Local local = new Local();

    @Getter
    @Setter
    public static class Local {

        /**
         * 本地文件保存根目录。
         */
        private String basePath = "uploads";

        /**
         * 返回给调用方和数据库记录使用的访问路径前缀。
         */
        private String urlPrefix = "/uploads";
    }
}
