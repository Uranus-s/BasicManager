package com.basic.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

public class GenerateMain {
    public static void main(String[] args) {

        String projectRoot = System.getProperty("user.dir"); // basic-generator
        String genRoot = projectRoot + "/gen";

        String javaOut = genRoot + "/java";
        String resourceOut = genRoot + "/resources/mapper";

        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/basic_project?useSSL=false&serverTimezone=Asia/Shanghai",
                        "root",
                        "123456"
                )
                .globalConfig(builder -> {
                    builder.author("aber")
                            .disableOpenDir()
                            .commentDate("yyyy-MM-dd")
                            .outputDir(javaOut);   // ⭐ 关键：全局java输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.basic")
                            .entity("entity")
                            .mapper("mapper")
                            .pathInfo(java.util.Map.of(
                                    OutputFile.xml, resourceOut
                            ));
                })
                .strategyConfig(builder -> {
//                    builder.addInclude("sys_user", "sys_role", "sys_permission");
//                    builder.addTablePrefix("sys_");

                    builder.entityBuilder()
                            .enableLombok();

                    builder.mapperBuilder()
                            .enableMapperAnnotation();
                })
                .templateConfig(builder -> {
                    builder.disable(TemplateType.SERVICE);
                    builder.disable(TemplateType.SERVICE_IMPL);
                    builder.disable(TemplateType.CONTROLLER);
                })
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }

}
