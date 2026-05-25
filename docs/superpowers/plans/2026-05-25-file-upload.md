# File Upload Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a real multipart file upload endpoint that stores files locally today and keeps the storage layer extensible for OSS/MinIO later.

**Architecture:** Keep `SysFileController` as the API entry and `SysFileServiceImpl` as the metadata/business orchestrator. Add a small storage strategy interface in `basic-service`, with `LocalFileStorageServiceImpl` as the first implementation selected by `basic.file.storage-type`.

**Tech Stack:** Java 23, Spring Boot 4.0.0, Spring MVC `MultipartFile`, MyBatis-Plus, JUnit 5, Mockito, AssertJ.

---

## File Structure

- Create `basic-service/src/main/java/com/basic/sericve/sysFile/storage/FileStorageService.java`: storage strategy interface.
- Create `basic-service/src/main/java/com/basic/sericve/sysFile/storage/StoredFile.java`: immutable storage result model.
- Create `basic-service/src/main/java/com/basic/sericve/sysFile/config/FileStorageProperties.java`: `basic.file` configuration binding.
- Create `basic-service/src/main/java/com/basic/sericve/sysFile/storage/LocalFileStorageServiceImpl.java`: local disk implementation.
- Modify `basic-service/src/main/java/com/basic/sericve/sysFile/service/ISysFileService.java`: add `FileVO uploadFile(MultipartFile file, String bizType)`.
- Modify `basic-service/src/main/java/com/basic/sericve/sysFile/impl/SysFileServiceImpl.java`: inject storage strategies, select configured storage, validate input, store file, save metadata, return `FileVO`.
- Modify `basic-api/src/main/java/com/basic/api/controller/sys/SysFileApi.java`: add `/upload` endpoint returning `FileVO`.
- Modify `basic-web/src/main/java/com/basic/web/controller/sys/SysFileController.java`: implement `/upload`.
- Modify `basic-web/src/main/resources/application.yml`: add default `basic.file` config.
- Create `basic-web/src/test/java/com/basic/web/sys/LocalFileStorageServiceImplTest.java`: local storage behavior tests.
- Create `basic-web/src/test/java/com/basic/web/sys/SysFileServiceImplUploadTest.java`: service upload behavior tests.
- Create `basic-web/src/test/java/com/basic/web/controller/SysFileControllerTest.java`: controller delegation test.

---

### Task 1: Add Local Storage Strategy

**Files:**
- Create: `basic-service/src/main/java/com/basic/sericve/sysFile/storage/FileStorageService.java`
- Create: `basic-service/src/main/java/com/basic/sericve/sysFile/storage/StoredFile.java`
- Create: `basic-service/src/main/java/com/basic/sericve/sysFile/config/FileStorageProperties.java`
- Create: `basic-service/src/main/java/com/basic/sericve/sysFile/storage/LocalFileStorageServiceImpl.java`
- Test: `basic-web/src/test/java/com/basic/web/sys/LocalFileStorageServiceImplTest.java`

- [ ] **Step 1: Write the failing test for local file storage**

```java
package com.basic.web.sys;

import com.basic.sericve.sysFile.config.FileStorageProperties;
import com.basic.sericve.sysFile.storage.LocalFileStorageServiceImpl;
import com.basic.sericve.sysFile.storage.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void storeWritesFileUnderBizTypeDateDirectoryWithoutUsingOriginalNameAsDiskName() throws Exception {
        FileStorageProperties properties = new FileStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        properties.getLocal().setUrlPrefix("/uploads");
        LocalFileStorageServiceImpl storageService = new LocalFileStorageServiceImpl(properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "file-content".getBytes()
        );

        StoredFile storedFile = storageService.store(file, "avatar");

        assertThat(storedFile.getFileName()).isEqualTo("avatar.png");
        assertThat(storedFile.getFileSize()).isEqualTo(12L);
        assertThat(storedFile.getFileType()).isEqualTo("image/png");
        assertThat(storedFile.getFilePath()).startsWith("/uploads/avatar/");
        assertThat(storedFile.getFilePath()).endsWith(".png");
        assertThat(storedFile.getFilePath()).doesNotEndWith("/avatar.png");
        Path savedPath = tempDir.resolve(storedFile.getFilePath().replaceFirst("^/uploads/", ""));
        assertThat(Files.readString(savedPath)).isEqualTo("file-content");
    }

    @Test
    void storeRejectsInvalidBizType() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        LocalFileStorageServiceImpl storageService = new LocalFileStorageServiceImpl(properties);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());

        assertThatThrownBy(() -> storageService.store(file, "../avatar"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bizType");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl basic-web -Dtest=LocalFileStorageServiceImplTest test`

Expected: FAIL because `LocalFileStorageServiceImpl`, `FileStorageProperties`, and `StoredFile` do not exist.

- [ ] **Step 3: Write minimal implementation**

Create `FileStorageService.java`:

```java
package com.basic.sericve.sysFile.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storageType();

    StoredFile store(MultipartFile file, String bizType);

    void delete(String filePath);
}
```

Create `StoredFile.java`:

```java
package com.basic.sericve.sysFile.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {

    private final String fileName;

    private final String filePath;

    private final Long fileSize;

    private final String fileType;
}
```

Create `FileStorageProperties.java`:

```java
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
```

Create `LocalFileStorageServiceImpl.java`:

```java
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

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileStorageProperties.class)
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Pattern BIZ_TYPE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl basic-web -Dtest=LocalFileStorageServiceImplTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add basic-service/src/main/java/com/basic/sericve/sysFile/storage/FileStorageService.java basic-service/src/main/java/com/basic/sericve/sysFile/storage/StoredFile.java basic-service/src/main/java/com/basic/sericve/sysFile/config/FileStorageProperties.java basic-service/src/main/java/com/basic/sericve/sysFile/storage/LocalFileStorageServiceImpl.java basic-web/src/test/java/com/basic/web/sys/LocalFileStorageServiceImplTest.java
git commit -m "feat(file): add local storage strategy"
```

---

### Task 2: Add Service Upload Orchestration

**Files:**
- Modify: `basic-service/src/main/java/com/basic/sericve/sysFile/service/ISysFileService.java`
- Modify: `basic-service/src/main/java/com/basic/sericve/sysFile/impl/SysFileServiceImpl.java`
- Test: `basic-web/src/test/java/com/basic/web/sys/SysFileServiceImplUploadTest.java`

- [ ] **Step 1: Write failing service tests**

```java
package com.basic.web.sys;

import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.exception.BusinessException;
import com.basic.dao.sysFile.entity.SysFile;
import com.basic.dao.sysFile.mapper.SysFileMapper;
import com.basic.sericve.sysFile.config.FileStorageProperties;
import com.basic.sericve.sysFile.impl.SysFileServiceImpl;
import com.basic.sericve.sysFile.storage.FileStorageService;
import com.basic.sericve.sysFile.storage.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysFileServiceImplUploadTest {

    @Mock
    private SysFileMapper sysFileMapper;

    @Mock
    private FileStorageService storageService;

    @Test
    void uploadFileStoresFileSavesMetadataAndReturnsFileVO() {
        FileStorageProperties properties = new FileStorageProperties();
        SysFileServiceImpl service = new SysFileServiceImpl(List.of(storageService), properties);
        ReflectionTestUtils.setField(service, "baseMapper", sysFileMapper);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());
        when(storageService.storageType()).thenReturn("local");
        when(storageService.store(file, "avatar")).thenReturn(
                new StoredFile("avatar.png", "/uploads/avatar/2026/05/25/file.png", 7L, "image/png")
        );
        when(sysFileMapper.insert(any(SysFile.class))).thenAnswer(invocation -> {
            SysFile sysFile = invocation.getArgument(0);
            sysFile.setId(99L);
            return 1;
        });

        FileVO result = service.uploadFile(file, "avatar");

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getFileName()).isEqualTo("avatar.png");
        assertThat(result.getFilePath()).isEqualTo("/uploads/avatar/2026/05/25/file.png");
        assertThat(result.getFileSize()).isEqualTo(7L);
        assertThat(result.getFileType()).isEqualTo("image/png");
        assertThat(result.getBizType()).isEqualTo("avatar");
        ArgumentCaptor<SysFile> fileCaptor = ArgumentCaptor.forClass(SysFile.class);
        verify(sysFileMapper).insert(fileCaptor.capture());
        assertThat(fileCaptor.getValue().getFileName()).isEqualTo("avatar.png");
        assertThat(fileCaptor.getValue().getFilePath()).isEqualTo("/uploads/avatar/2026/05/25/file.png");
    }

    @Test
    void uploadFileRejectsEmptyFile() {
        FileStorageProperties properties = new FileStorageProperties();
        SysFileServiceImpl service = new SysFileServiceImpl(List.of(storageService), properties);
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> service.uploadFile(file, "docs"))
                .isInstanceOf(BusinessException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl basic-web -Dtest=SysFileServiceImplUploadTest test`

Expected: FAIL because constructor and `uploadFile(MultipartFile, String)` do not exist.

- [ ] **Step 3: Write minimal implementation**

Modify `ISysFileService.java` to add:

```java
FileVO uploadFile(MultipartFile file, String bizType);
```

Add import:

```java
import org.springframework.web.multipart.MultipartFile;
```

Modify `SysFileServiceImpl.java`:

```java
private final Map<String, FileStorageService> storageServiceMap;

private final FileStorageProperties fileStorageProperties;

public SysFileServiceImpl(List<FileStorageService> storageServices, FileStorageProperties fileStorageProperties) {
    this.storageServiceMap = storageServices.stream()
            .collect(Collectors.toMap(FileStorageService::storageType, Function.identity()));
    this.fileStorageProperties = fileStorageProperties;
}
```

Add upload implementation:

```java
@Override
@Transactional(rollbackFor = Exception.class)
public FileVO uploadFile(MultipartFile file, String bizType) {
    if (file == null || file.isEmpty()) {
        throw new BusinessException(ResultEnum.PARAM_ERROR);
    }
    if (!StringUtils.hasText(bizType)) {
        throw new BusinessException(ResultEnum.PARAM_ERROR);
    }

    FileStorageService storageService = storageServiceMap.get(fileStorageProperties.getStorageType());
    if (storageService == null) {
        throw new BusinessException(ResultEnum.PARAM_ERROR);
    }

    StoredFile storedFile;
    try {
        storedFile = storageService.store(file, bizType);
    } catch (RuntimeException ex) {
        throw new BusinessException(ResultEnum.ERROR);
    }

    SysFile sysFile = new SysFile();
    sysFile.setFileName(storedFile.getFileName());
    sysFile.setFilePath(storedFile.getFilePath());
    sysFile.setFileSize(storedFile.getFileSize());
    sysFile.setFileType(storedFile.getFileType());
    sysFile.setBizType(bizType);

    try {
        save(sysFile);
    } catch (RuntimeException ex) {
        storageService.delete(storedFile.getFilePath());
        throw ex;
    }

    FileVO vo = new FileVO();
    BeanUtils.copyProperties(sysFile, vo);
    return vo;
}
```

Required imports:

```java
import com.basic.sericve.sysFile.config.FileStorageProperties;
import com.basic.sericve.sysFile.storage.FileStorageService;
import com.basic.sericve.sysFile.storage.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl basic-web -Dtest=SysFileServiceImplUploadTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add basic-service/src/main/java/com/basic/sericve/sysFile/service/ISysFileService.java basic-service/src/main/java/com/basic/sericve/sysFile/impl/SysFileServiceImpl.java basic-web/src/test/java/com/basic/web/sys/SysFileServiceImplUploadTest.java
git commit -m "feat(file): add upload orchestration"
```

---

### Task 3: Add API and Controller Upload Endpoint

**Files:**
- Modify: `basic-api/src/main/java/com/basic/api/controller/sys/SysFileApi.java`
- Modify: `basic-web/src/main/java/com/basic/web/controller/sys/SysFileController.java`
- Test: `basic-web/src/test/java/com/basic/web/controller/SysFileControllerTest.java`

- [ ] **Step 1: Write failing controller test**

```java
package com.basic.web.controller;

import com.basic.api.vo.sysFile.FileVO;
import com.basic.common.result.Result;
import com.basic.sericve.sysFile.service.ISysFileService;
import com.basic.web.controller.sys.SysFileController;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SysFileControllerTest {

    @Test
    void uploadDelegatesMultipartFileToServiceAndReturnsFileVO() {
        ISysFileService sysFileService = mock(ISysFileService.class);
        SysFileController controller = new SysFileController(sysFileService);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());
        FileVO fileVO = new FileVO();
        fileVO.setId(10L);
        fileVO.setFileName("avatar.png");
        when(sysFileService.uploadFile(file, "avatar")).thenReturn(fileVO);

        Result<FileVO> result = controller.upload(file, "avatar");

        verify(sysFileService).uploadFile(file, "avatar");
        assertThat(result.getData().getId()).isEqualTo(10L);
        assertThat(result.getData().getFileName()).isEqualTo("avatar.png");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl basic-web -Dtest=SysFileControllerTest test`

Expected: FAIL because `SysFileController.upload(MultipartFile, String)` does not exist.

- [ ] **Step 3: Write minimal API and controller implementation**

Modify `SysFileApi.java`:

```java
@PostMapping("/upload")
Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                      @RequestParam("bizType") String bizType);
```

Add import:

```java
import org.springframework.web.multipart.MultipartFile;
```

Modify `SysFileController.java`:

```java
@Override
@PostMapping("/upload")
@OperateLog(module = "文件管理", method = "上传文件")
public Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                             @RequestParam("bizType") String bizType) {
    return Result.success(sysFileService.uploadFile(file, bizType));
}
```

Add import:

```java
import org.springframework.web.multipart.MultipartFile;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl basic-web -Dtest=SysFileControllerTest test`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add basic-api/src/main/java/com/basic/api/controller/sys/SysFileApi.java basic-web/src/main/java/com/basic/web/controller/sys/SysFileController.java basic-web/src/test/java/com/basic/web/controller/SysFileControllerTest.java
git commit -m "feat(file): add multipart upload endpoint"
```

---

### Task 4: Add Configuration and Verify Full Build

**Files:**
- Modify: `basic-web/src/main/resources/application.yml`

- [ ] **Step 1: Add default file configuration**

Append to `application.yml`:

```yaml
basic:
  file:
    storage-type: local
    local:
      base-path: ${BASIC_FILE_LOCAL_BASE_PATH:uploads}
      url-prefix: ${BASIC_FILE_LOCAL_URL_PREFIX:/uploads}
```

If `basic:` already exists later, merge the `file` block under it rather than duplicating the root key.

- [ ] **Step 2: Run focused upload tests**

Run: `mvn -pl basic-web -Dtest=LocalFileStorageServiceImplTest,SysFileServiceImplUploadTest,SysFileControllerTest test`

Expected: PASS.

- [ ] **Step 3: Run full build without tests skipped**

Run: `mvn test`

Expected: PASS. If environment-dependent existing tests fail because MySQL or Redis is unavailable, record the exact failing tests and then run the focused upload tests as the verification evidence for this feature.

- [ ] **Step 4: Commit**

```bash
git add basic-web/src/main/resources/application.yml
git commit -m "chore(file): configure local upload defaults"
```

---

### Task 5: Final Review

**Files:**
- Review all files changed by Tasks 1-4.

- [ ] **Step 1: Inspect final diff**

Run: `git diff HEAD~4..HEAD --stat`

Expected: shows only file upload design and implementation files.

- [ ] **Step 2: Verify requirements against spec**

Checklist:

- `/system/file/upload` exists and returns `Result<FileVO>`.
- Upload accepts `MultipartFile file` and `String bizType`.
- `SysFileServiceImpl` validates empty file and blank `bizType`.
- Storage strategy interface exists.
- Local strategy stores under `{base-path}/{bizType}/{yyyy}/{MM}/{dd}/{uuid}.{ext}`.
- Local strategy records `filePath` using `url-prefix`.
- Existing metadata record endpoint remains available.
- Focused tests pass.

- [ ] **Step 3: Report verification evidence**

Report:

- Commit hashes created.
- Exact test commands run.
- Whether `mvn test` passed or which environment-dependent tests failed.
