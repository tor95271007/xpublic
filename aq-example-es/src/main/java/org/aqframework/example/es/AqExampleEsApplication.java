package org.aqframework.example.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.aqframework")
public class AqExampleEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AqExampleEsApplication.class);
    }
}
