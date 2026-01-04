package com.ryr.hospital.repository;

import com.ryr.hospital.model.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepo extends MongoRepository<Patient, String> {
}
