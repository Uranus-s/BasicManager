# Basic-DAO 模块说明

## 1. 模块定位

`basic-dao` 是项目的 **数据访问模块**，负责与数据库交互，包含 Entity 实体类和 Mapper 接口。

主要职责：
- Entity 实体类定义（数据库表映射）
- Mapper 接口定义（数据库操作）
- 数据库表结构管理

> 📌 最底层业务模块：只负责数据持久化，无业务逻辑

---

## 2. 模块结构

```text
basic-dao
 └── com.basic.dao
     ├── sysUser/           # 用户
     │   ├── entity/
     │   │   └── SysUser.java    # 用户实体
     │   └── mapper/
     │       └── SysUserMapper.java
     ├── sysRole/           # 角色
     │   ├── entity/
     │   └── mapper/
     ├── sysPermission/     # 权限
     │   ├── entity/
     │   └── mapper/
     ├── sysDept/           # 部门
     │   ├── entity/
     │   └── mapper/
     ├── sysDict/           # 字典
     │   ├── entity/
     │   └── mapper/
     ├── sysDictItem/      # 字典项
     │   ├── entity/
     │   └── mapper/
     ├── sysConfig/        # 配置
     │   ├── entity/
     │   └── mapper/
     ├── sysFile/          # 文件
     │   ├── entity/
     │   └── mapper/
     ├── sysLoginLog/      # 登录日志
     │   ├── entity/
     │   └── mapper/
     ├── sysOperLog/       # 操作日志
     │   ├── entity/
     │   └── mapper/
     ├── sysUserRole/      # 用户角色关联
     │   ├── entity/
     │   └── mapper/
     ├── sysUserDept/      # 用户部门关联
     │   ├── entity/
     │   └── mapper/
     └── sysRolePermission/ # 角色权限关联
         ├── entity/
         └── mapper/
```

---

## 3. Entity 规范

### 3.1 Entity 类结构

继承自 `BaseEntity`（在 basic-core 中定义），包含以下公共字段：

```java
@TableName("sys_user")
public class SysUser extends BaseEntity {
    // 公共字段（继承自 BaseEntity）
    // id, createTime, updateTime, createBy, updateBy, version, deleted

    // 业务字段
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
}
```

### 3.2 实体类规范

| 规范 | 说明 |
|------|------|
| 注解 | @TableName("表名") |
| 主键 | @TableId(value = "id", type = IdType.AUTO) |
| 命名 | 驼峰命名，数据库下划线映射 |
| 继承 | 继承 BaseEntity 获取公共字段 |

---

## 4. Mapper 规范

### 4.1 Mapper 接口

```java
public interface SysUserMapper extends BaseMapperPlus<SysUser, SysUserMapper> {
    // 自定义方法
}
```

### 4.2 BaseMapperPlus 方法

继承自 MyBatis-Plus 的 `IService`，提供常用方法：

| 方法 | 说明 |
|------|------|
| insert(entity) | 插入 |
| updateById(entity) | 更新 |
| deleteById(id) | 删除（逻辑删除） |
| selectById(id) | 查询 |
| selectBatchIds(ids) | 批量查询 |
| selectList(queryWrapper) | 条件查询 |

---

## 5. Maven 依赖

```xml
<dependencies>
    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
    </dependency>

    <!-- 项目模块 -->
    <dependency>
        <groupId>com.basic</groupId>
        <artifactId>basic-core</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 6. 模块依赖

### 依赖关系

```
basic-dao
    └── basic-core    # 依赖 BaseEntity、BaseMapperPlus
```

### 被依赖

| 模块 | 说明 |
|------|------|
| basic-common-core | 无依赖 |
| basic-common-web | 无依赖 |
| basic-core | 无依赖 |
| basic-api | 无依赖 |
| basic-service | 依赖 |
| basic-web | 无依赖 |

---

## 7. 数据流

```
Controller → Service → Mapper → Database
            (Entity)   (Entity)
```

### 数据转换

```
DTO (请求) → Entity (持久化) → BO (业务) → VO (响应)
```
