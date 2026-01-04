package com.ryr.hospital;

import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate() {
        // 1. We HARDCODE the connection string here.
        // This bypasses the broken properties file completely.
        String connectionString = "mongodb+srv://yosu_lands:tuturyr12@cluster0.cigmkxk.mongodb.net/HospitalDB?appName=Cluster0";
        
        // 2. We force the connection
        System.out.println("🔥 FORCING CONNECTION TO: " + connectionString);
        return new MongoTemplate(MongoClients.create(connectionString), "HospitalDB");
    }
}