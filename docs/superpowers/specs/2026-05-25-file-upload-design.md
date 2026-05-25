# 文件上传功能设计

## 背景

当前项目已有文件管理业务线，包括 `SysFileApi`、`SysFileController`、`ISysFileService`、`SysFileServiceImpl`、`SysFile` 实体和 `sys_file` 表。现有上传接口只接收文件名、路径、大小、类型和业务类型，用于登记文件元数据，没有真正接收 `MultipartFile`，也没有文件存储抽象。

本次新增真实文件上传能力，第一阶段只实现本地存储，同时保留后续扩展 OSS、MinIO 等存储方式的边界。

## 目标

- 新增真实上传接口，接收 `multipart/form-data` 文件。
- 上传成功后写入 `sys_file` 记录，并返回 `FileVO`。
- 存储能力通过策略接口隔离，当前默认使用本地存储。
- 后续新增 OSS、MinIO 时，只新增存储实现和配置选择，不改 Controller 调用方式。
- 遵守现有模块依赖：`web -> api -> service -> dao -> core/common-core`。

## 非目标

- 本次不实现 OSS 或 MinIO 客户端。
- 本次不调整 `sys_file` 表结构。
- 本次不实现复杂文件安全扫描、分片上传、秒传、断点续传。
- 本次不强制删除历史“上传文件记录”接口，避免破坏现有调用方。

## 接口设计

在 `SysFileApi` 中新增真实上传接口：

```java
@PostMapping("/upload")
Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                      @RequestParam("bizType") String bizType);
```

`SysFileController` 实现该接口：

```text
POST /system/file/upload
Content-Type: multipart/form-data

file: 上传文件
bizType: 业务类型
```

响应使用项目统一 `Result<FileVO>`。`FileVO` 至少包含已有字段：`id`、`fileName`、`filePath`、`fileSize`、`fileType`、`bizType`、`createTime`。

保留现有 `POST /system/file` 元数据登记接口，方法名后续可调整为更清晰的 `createFileRecord`，但本次不做破坏性修改。

## 存储抽象

在 `basic-service` 内新增文件存储策略接口，避免 `SysFileServiceImpl` 直接耦合本地磁盘、OSS 或 MinIO：

```java
public interface FileStorageService {
    String storageType();

    StoredFile store(MultipartFile file, String bizType);

    void delete(String filePath);
}
```

新增存储结果模型：

```java
public class StoredFile {
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
}
```

`storageType()` 用于标识实现类型，例如 `local`、`oss`、`minio`。第一阶段只提供 `local` 实现。

## 本地存储实现

新增 `LocalFileStorageServiceImpl`，职责只包含本地落盘和本地文件删除。

建议保存路径：

```text
{base-path}/{bizType}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
```

示例：

```text
uploads/avatar/2026/05/25/9f0c2f1d9d0b4e85a0c6d6f75b9a0a7f.png
```

返回给数据库的 `filePath` 使用访问路径前缀：

```text
{url-prefix}/{bizType}/{yyyy}/{MM}/{dd}/{uuid}.{ext}
```

示例：

```text
/uploads/avatar/2026/05/25/9f0c2f1d9d0b4e85a0c6d6f75b9a0a7f.png
```

为避免路径穿越，`bizType` 只允许字母、数字、下划线和短横线。原始文件名只用于记录 `fileName`，不直接参与磁盘文件名。

## 配置设计

在 `application.yml` 增加默认配置：

```yaml
basic:
  file:
    storage-type: local
    local:
      base-path: uploads
      url-prefix: /uploads
```

新增配置类：

```java
@ConfigurationProperties(prefix = "basic.file")
public class FileStorageProperties {
    private String storageType = "local";
    private Local local = new Local();

    public static class Local {
        private String basePath = "uploads";
        private String urlPrefix = "/uploads";
    }
}
```

`SysFileServiceImpl` 通过配置选择 `FileStorageService`。第一阶段如果配置为非 `local` 且没有对应实现，应启动失败或在上传时抛出明确业务异常。推荐启动时完成策略映射校验，尽早暴露配置错误。

## 业务流程

```text
SysFileController.upload
-> ISysFileService.uploadFile(MultipartFile file, String bizType)
-> 校验文件和 bizType
-> 选择 FileStorageService
-> 存储文件，得到 StoredFile
-> 写入 SysFile
-> 转换并返回 FileVO
```

如果数据库保存失败，第一版不引入跨资源事务。`SysFileServiceImpl` 应在保存记录失败时尽量调用 `FileStorageService.delete(filePath)` 清理已落盘文件，再抛出异常。

## 错误处理

- `file == null` 或 `file.isEmpty()`：抛出 `BusinessException`。
- `bizType` 为空或包含非法字符：抛出 `BusinessException`。
- 文件名为空：使用默认文件名或抛出 `BusinessException`。推荐抛出异常，避免记录不可读。
- 落盘失败：抛出 `BusinessException`。
- 配置的存储类型不存在：抛出 `BusinessException` 或启动阶段抛出配置错误。

错误响应继续走现有 `GlobalExceptionAdvice`。

## 测试设计

按 TDD 实施，优先覆盖服务层行为：

- 空文件上传失败。
- 非法 `bizType` 上传失败。
- 合法文件上传会调用存储策略、保存 `SysFile`，并返回 `FileVO`。
- 本地存储会创建日期目录并写入文件。
- 本地存储不会使用原始文件名作为磁盘文件名。

Controller 层补充轻量单元测试：

- `upload` 接口把 `MultipartFile` 和 `bizType` 传给 service，并返回 `FileVO`。

## 扩展方式

后续新增 OSS 或 MinIO 时：

1. 新增 `OssFileStorageServiceImpl` 或 `MinioFileStorageServiceImpl`。
2. 实现 `FileStorageService`。
3. 在配置中设置 `basic.file.storage-type=oss` 或 `minio`。
4. 复用 `SysFileServiceImpl` 的上传编排和 `SysFileController` 的接口。

这样可以保持 API 与业务入口稳定，降低替换存储实现的影响范围。
