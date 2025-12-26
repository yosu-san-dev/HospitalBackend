package com.ryr.hospital.service;

import com.ryr.hospital.model.Patient;
import com.ryr.hospital.repository.PatientRepo;
import com.ryr.hospital.util.JwtUtil;
import com.ryr.hospital.util.SecurityUtil;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private final PatientRepo patientRepo;
    private final JwtUtil jwtUtil;

    public AuthService(PatientRepo patientRepo, JwtUtil jwtUtil) {
        this.patientRepo = patientRepo;
        this.jwtUtil = jwtUtil;
    }

    public void register(String fulName, String rawTC, String rawPassword) throws Exception {

        String hashedTC = SecurityUtil.hashTC(rawTC);

        if (patientRepo.existsById(hashedTC)) {
            throw new Exception("Error: a patient with this TC already exists.");
        }

        String hashedPass = SecurityUtil.hashPassword(rawPassword);

        Patient newPatient = new Patient(fulName, hashedTC, hashedPass);
        patientRepo.save(newPatient);

        System.out.println("Patient registered successfully: " + fulName);
    }

    public String login(String rawTC, String rawPassword) {
        String hashedTC = SecurityUtil.hashTC(rawTC);

        Optional<Patient> patientOpt = patientRepo.findById(hashedTC);

        if(patientOpt.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        Patient patient = patientOpt.get();

        if(SecurityUtil.chackPassword(rawPassword, patient.getHashedPassword())) {
            return jwtUtil.generateToken(patient.getId(), patient.getRole());
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
