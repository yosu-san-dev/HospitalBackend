package com.ryr.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Collections;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // 1. Force the Database URL directly here
        String mongoUri = "mongodb+srv://yosu_lands:tuturyr12@cluster0.cigmkxk.mongodb.net/HospitalDB?appName=Cluster0";
        System.setProperty("spring.data.mongodb.uri", mongoUri);

        // 2. Force the Database Name
        System.setProperty("spring.data.mongodb.database", "HospitalDB");

        // 3. Run the app
        SpringApplication app = new SpringApplication(Application.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "8080"));
        app.run(args);
    }
}