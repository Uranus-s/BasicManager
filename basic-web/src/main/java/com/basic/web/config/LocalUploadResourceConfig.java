package com.basic.web.config;

import com.basic.sericve.sysFile.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * 将本地上传目录暴露为静态资源，供头像等文件通过返回的 filePath 直接访问。
 * <p>
 * 文件上传服务保存到 basic.file.local.base-path，并返回 basic.file.local.url-prefix 开头的访问路径。
 * Spring Boot 默认只暴露 classpath 下的静态资源，不会自动暴露项目运行目录下的 uploads 目录，
 * 因此需要在 Web 层显式注册 ResourceHandler。
 * </p>
 * <p>
 * 该配置只在 storage-type=local 时启用。后续切换到 OSS、MinIO 等对象存储后，
 * 文件应由对象存储或 CDN 提供访问，不能继续暴露本地上传目录。
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(FileStorageProperties.class)
@ConditionalOnProperty(prefix = "basic.file", name = "storage-type", havingValue = "local", matchIfMissing = true)
public class LocalUploadResourceConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPrefix = normalizeUrlPrefix(fileStorageProperties.getLocal().getUrlPrefix());
        Path basePath = Path.of(fileStorageProperties.getLocal().getBasePath()).toAbsolutePath().normalize();
        // 例如：/uploads/** -> file:///G:/Java/BasicProject/uploads/
        // 这样数据库中保存的 /uploads/avatar/xxx.jpg 才能作为静态资源直接访问。
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(toDirectoryLocation(basePath));
    }

    /**
     * 将访问前缀统一成 /uploads 这种格式，避免配置成 uploads、/uploads/ 或反斜杠路径时匹配失败。
     */
    private String normalizeUrlPrefix(String urlPrefix) {
        String prefix = StringUtils.hasText(urlPrefix) ? urlPrefix.replace("\\", "/") : "/uploads";
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    /**
     * ResourceHandler 的文件目录 location 必须以 / 结尾，否则可能被当作单个文件路径处理。
     */
    private String toDirectoryLocation(Path basePath) {
        String location = basePath.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
