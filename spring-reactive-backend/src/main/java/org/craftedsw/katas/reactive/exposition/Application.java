package org.craftedsw.katas.reactive.exposition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.craftedsw.katas.reactive")
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
