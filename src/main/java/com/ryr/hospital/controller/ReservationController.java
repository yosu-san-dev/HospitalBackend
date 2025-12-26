package com.ryr.hospital.controller;

import com.ryr.hospital.model.Reservation;
import com.ryr.hospital.service.ReservationService;
import com.ryr.hospital.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final JwtUtil jwtUtil;

    public ReservationController(ReservationService reservationService, JwtUtil jwtUtil) {
        this.reservationService = reservationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/create")
    public String createReservation(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> body) {
        try {

            if (token == null || !token.startsWith("Bearer ")) {
                throw new RuntimeException("Missing or Invalid Token");
            }

            String realToken = token.substring(7);
            String petientId = jwtUtil.extractUserId(realToken);

            String doctorId = body.get("doctorId");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(body.get("startTime"), formatter);
            LocalDateTime end = LocalDateTime.parse(body.get("endTime"), formatter);

            reservationService.createReser(petientId, doctorId, start, end);
            return "Reservation confirmed!";

        } catch(Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @PostMapping("/my-appointments")
    public List<Reservation> getMyAppts(@RequestHeader("Authorization") String token) {
        try {
            String realToken = token.substring(7);
            String patientId = jwtUtil.extractUserId(realToken);
            
            return reservationService.getForPatient(patientId);
            
        } catch (Exception e) {
            return null;
        }
    } 
}
