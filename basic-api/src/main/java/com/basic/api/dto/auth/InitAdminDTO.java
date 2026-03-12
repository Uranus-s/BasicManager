package com.basic.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 初始化管理员请求DTO
 *
 * @author Gas
 */
@Data
public class InitAdminDTO {

    /**
     * 初始化密钥
     */
    @NotBlank(message = "初始化密钥不能为空")
    private String initKey;

    /**
     * 管理员密码
     */
    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String adminPassword;
}
