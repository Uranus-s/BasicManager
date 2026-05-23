package com.basic.api.vo.auth;

import lombok.Data;

/**
 * Token 响应 VO
 *
 * @author Gas
 */
@Data
public class TokenVO {

    /**
     * JWT Token
     */
    private String token;
}
