package com.vaas.templateengine;

import org.springframework.boot.SpringApplication;

public class TestTemplateEngineApplication {

    public static void main(String[] args) {
        SpringApplication.from(TemplateEngineApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
