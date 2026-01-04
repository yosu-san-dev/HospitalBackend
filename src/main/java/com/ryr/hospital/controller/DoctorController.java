package com.ryr.hospital.controller;

import com.ryr.hospital.model.Doctor;
import com.ryr.hospital.repository.DoctorRepo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@CrossOrigin(origins = "*") // This allows your Frontend to talk to it!
public class DoctorController {

    private final DoctorRepo doctorRepo;

    public DoctorController(DoctorRepo doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    // This creates the link: http://localhost:8080/doctors/all
    @GetMapping("/all")
    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }
}