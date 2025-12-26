package com.ryr.hospital.repository;

import com.ryr.hospital.model.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepo extends MongoRepository<Reservation, String>{
    
    // Spesifik bir doktorun tum randevularini bulur
    List<Reservation> findByDoctorId(String doctorId);

    // Spesifik bir hastanin tum randevularini bulur
    List<Reservation> findByPatientId(String patientId);
}
