package com.basic.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 测试示例
 *
 * @author Gas
 */
@RequestMapping("/test")
public interface TestApi {
    @GetMapping("/{id}")
    String getTest(@PathVariable Long id);
}
