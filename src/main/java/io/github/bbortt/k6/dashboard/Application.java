package io.github.bbortt.k6.dashboard;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        run(Application.class, args);
    }
}