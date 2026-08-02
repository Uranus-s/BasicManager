package com.basic.api.vo.sysConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公开系统设置视图，仅包含无需认证即可读取的展示信息。
 *
 * @author Gas
 */
@Data
@Schema(description = "公开系统设置")
public class PublicConfigVO {

    /**
     * 系统名称，供登录页等公开页面展示。
     */
    @Schema(description = "系统名称", example = "基础管理系统")
    private String systemName;
}
