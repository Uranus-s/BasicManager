package com.basic.web.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController(value = "testController")
@RequestMapping("/test")
@Validated
public class TestController {

    @GetMapping("/hello/{num}")
    public String hello(@PathVariable @Min(value = 1, message = "num must be greater than or equal to 1") @Max(value = 10, message = "num must be less than or equal to 10") Integer num) {
        return "hello world " + num;
    }

    @GetMapping("/getEmail")
    public String getEmail(@RequestParam @NotBlank @Email String email) {
        return email;
    }
}
