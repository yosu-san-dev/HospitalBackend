package com.ryr.hospital.service;

import com.ryr.hospital.model.Reservation;
import com.ryr.hospital.repository.ReservationRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepo reservationRepo;

    public ReservationService(ReservationRepo reservationRepo) {
        this.reservationRepo = reservationRepo;
    }

    // Randevularin cakismasini control eder ve Boolen deger dondurur
    public boolean hasOverlap(String doctorId, LocalDateTime start, LocalDateTime end) {

        List<Reservation> doctorAppts = reservationRepo.findByDoctorId(doctorId);

        for(Reservation existing: doctorAppts){

            boolean overlap = start.isBefore(existing.getEndDT()) && end.isAfter(existing.getStartDT());

            if(overlap) return true;
        }
        return false;
    };

    //Randevu ekleme
    public Reservation createReser(String patientId, String doctorId, LocalDateTime start, LocalDateTime end) {

        if(hasOverlap(doctorId, start, end)) {
            throw new RuntimeException("This time slot is already taken!");
        }

        Reservation res = new Reservation(patientId, doctorId, start, end);
        return reservationRepo.save(res);
    }

    // Hastanin randevularini Get eder
    public List<Reservation> getForPatient(String patientId) {
        return reservationRepo.findByPatientId(patientId);
    }
}
