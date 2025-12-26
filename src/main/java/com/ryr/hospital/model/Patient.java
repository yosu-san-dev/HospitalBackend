package com.ryr.hospital.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "patients")
public class Patient {

    @Id
    private String id; // Bu hashed TC numararsidir

    private String fullName;
    private String hashedPassword;
    private String role;

    // Constructor'ler
    public Patient() {}

    public Patient(String fullName, String id, String hashedPassword) {
        this.fullName = fullName;
        this.id = id;
        this.hashedPassword = hashedPassword;
        this.role = "Patient";
    }

    // Get'ler ve Set'ler
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}