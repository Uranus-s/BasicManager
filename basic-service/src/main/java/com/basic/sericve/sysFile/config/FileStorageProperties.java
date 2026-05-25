package com.basic.sericve.sysFile.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "basic.file")
public class FileStorageProperties {

    private String storageType = "local";

    private Local local = new Local();

    @Getter
    @Setter
    public static class Local {

        private String basePath = "uploads";

        private String urlPrefix = "/uploads";
    }
}
