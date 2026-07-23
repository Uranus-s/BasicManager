package com.basic.api.vo.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Token 响应 VO
 *
 * @author Gas
 */
@Data
@Schema(description = "访问令牌信息")
public class TokenVO {

    /**
     * JWT Token
     */
    @Schema(description = "JWT 访问令牌", example = "eyJhbGciOiJIUzI1NiJ9.example.signature")
    private String token;
}
