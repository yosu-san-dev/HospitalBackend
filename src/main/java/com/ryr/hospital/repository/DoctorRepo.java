package com.ryr.hospital.repository;

import com.ryr.hospital.model.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoctorRepo extends MongoRepository<Doctor, String>{
    
    // "db.doctors.find({ branch: ?0 })"
    List<Doctor> findByBranch(String branch);
}
