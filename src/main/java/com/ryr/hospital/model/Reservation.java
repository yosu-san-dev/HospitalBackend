package com.ryr.hospital.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.Duration;

@Document(collection = "reservations")
public class Reservation{

    @Id
    private String id;

    private String patientId; // Kim
    private String doctorId; // Kimle

    private LocalDateTime startDT; // DT bir Date(tarih) Time(zaman) kısaltılması
    private LocalDateTime endDT;

    public Reservation() {}

    public Reservation(String patientId, String doctorId, LocalDateTime startDT, LocalDateTime endDT) {

        // Null deger kontrolu
        if(startDT == null || endDT == null) {
            throw new IllegalArgumentException("Time slots can not be NULL's!");
        }

        // Reservasyon sonlanma zamaninin baslama zamanindan sonra bir kontrol yapisi
        if(endDT.isBefore(startDT) || endDT.isEqual(startDT)) {
            throw new IllegalArgumentException("End time must be before start time!");
        }

        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startDT = startDT;
        this.endDT = endDT;
    }

    // Get'ler ve Set'ler
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getStartDT() { return startDT; }
    public void setStartDT(LocalDateTime startDT) { this.startDT = startDT; }

    public LocalDateTime getEndDT() { return endDT; }
    public void setEndDT(LocalDateTime endDT) { this.endDT = endDT; }

    public long getDurationMinutes() {
        return Duration.between(startDT, endDT).toMinutes();
    }
}
