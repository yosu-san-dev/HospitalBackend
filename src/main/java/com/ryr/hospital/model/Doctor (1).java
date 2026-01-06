package com.ryr.hospital.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "doctors")
public class Doctor {
    
    @Id
    private String id; // otomatik id atanacak

    private String name;
    private String branch;
    private int age;

    public Doctor() {}

    public Doctor(String name, String branch, int age) {
        this.name = name;
        this.branch = branch;
        this.age = age;
    }

    // Get'ler ve Set'ler
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

   
}
