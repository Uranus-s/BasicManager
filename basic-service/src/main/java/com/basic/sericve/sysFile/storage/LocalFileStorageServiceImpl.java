package com.basic.sericve.sysFile.storage;

import com.basic.sericve.sysFile.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 本地文件存储实现。
 * <p>
 * 文件保存到配置的 basePath 下，数据库记录使用 urlPrefix 拼出的访问路径。
 * 该类只负责本地磁盘读写，不处理文件业务记录。
 * </p>
 *
 * @author Gas
 */
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileStorageProperties.class)
public class LocalFileStorageServiceImpl implements FileStorageService {

    /**
     * 限制业务类型为安全路径片段，避免 ../ 这类路径穿越输入。
     */
    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    /**
     * 按日期分目录，避免单目录文件过多。
     */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final FileStorageProperties properties;

    @Override
    public String storageType() {
        return "local";
    }

    @Override
    public StoredFile store(MultipartFile file, String bizType) {
        validateBizType(bizType);
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("fileName must not be blank");
        }

        String extension = getExtension(originalFilename);
        // 磁盘文件名使用 UUID，避免原始文件名冲突或携带特殊字符。
        String storedFileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String relativePath = bizType + "/" + DATE_PATH_FORMATTER.format(LocalDate.now()) + "/" + storedFileName;
        Path targetPath = Path.of(properties.getLocal().getBasePath()).resolve(relativePath).normalize();

        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException("store file failed", ex);
        }

        String filePath = joinUrl(properties.getLocal().getUrlPrefix(), relativePath);
        String fileType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
        return new StoredFile(originalFilename, filePath, file.getSize(), fileType);
    }

    @Override
    public void delete(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        // 只删除当前 urlPrefix 管理下的文件，避免误删其他路径。
        String urlPrefix = normalizeUrlPrefix(properties.getLocal().getUrlPrefix());
        String normalizedFilePath = filePath.replace("\\", "/");
        if (!normalizedFilePath.startsWith(urlPrefix + "/")) {
            return;
        }
        String relativePath = normalizedFilePath.substring(urlPrefix.length() + 1);
        Path targetPath = Path.of(properties.getLocal().getBasePath()).resolve(relativePath).normalize();
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
        }
    }

    private void validateBizType(String bizType) {
        if (!StringUtils.hasText(bizType) || !BIZ_TYPE_PATTERN.matcher(bizType).matches()) {
            throw new IllegalArgumentException("bizType must only contain letters, numbers, underscores, or hyphens");
        }
    }

    private String getExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String joinUrl(String urlPrefix, String relativePath) {
        return normalizeUrlPrefix(urlPrefix) + "/" + relativePath.replace("\\", "/");
    }

    private String normalizeUrlPrefix(String urlPrefix) {
        String prefix = StringUtils.hasText(urlPrefix) ? urlPrefix.replace("\\", "/") : "/uploads";
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }
}
