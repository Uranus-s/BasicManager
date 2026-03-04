package com.basic.web.controller;

import com.basic.dao.sysUser.mapper.SysUserMapper;
import com.basic.sericve.sysUser.service.ISysUserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@RestController(value = "testController")
@RequestMapping("/test")
@Validated
public class TestController {

    @Autowired
    private ISysUserService sysUserService;

    @GetMapping("/hello/{num}")
    public String hello(@PathVariable("num") @Min(value = 1, message = "num must be greater than or equal to 1") @Max(value = 10, message = "num must be less than or equal to 10") Integer num) {
//        System.out.println("sysUserMapper = " + sysUserMapper);
//        sysUserMapper.getUserByName1("1");
//        sysUserMapper.getUserByName2("1");
//        sysUserMapper.selectById(1);
        sysUserService.getById(1);
        return "hello world " + num;
    }

    @GetMapping("/getEmail")
    public String getEmail(@RequestParam @NotBlank @Email String email) {
        return email;
    }

    @GetMapping("/getStock")
    public String getStock(){
        return CompletableFuture.supplyAsync(()->{
            // 查询库存
            return "stock";
        }, Executors.newVirtualThreadPerTaskExecutor()).join();
    }
}
