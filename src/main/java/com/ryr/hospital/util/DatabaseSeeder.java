package com.ryr.hospital.util;

import com.ryr.hospital.model.Doctor;
import com.ryr.hospital.repository.DoctorRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component // This tells Spring to detect and run this class
public class DatabaseSeeder implements CommandLineRunner {

    private final DoctorRepo doctorRepo;

    public DatabaseSeeder(DoctorRepo doctorRepo) {
        this.doctorRepo = doctorRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Check if database is already populated
        if (doctorRepo.count() > 0) {
            System.out.println("✅ Database already seeded. Skipping...");
            return;
        }

        // 2. Create the list of doctors (Based on your original code)
        List<Doctor> initialDoctors = List.of(
            // Cardiology (Kardiyoloji)
            new Doctor("Ahmet Yilmaz", "Kardiyoloji",27),
            new Doctor("Selin Karaca", "Kardiyoloji", 45),
            new Doctor("Murat Aydin", "Kardiyoloji", 35),

            // ENT (KBB)
            new Doctor("Mehmet Kaya", "KBB",40),
            new Doctor("Hakan Ozturk", "KBB", 50),
            new Doctor("Busra Aksoy", "KBB", 30),

            // Ophthalmology (Göz)
            new Doctor("Elif Demir", "Göz Hastalıkları", 56),
            new Doctor("Yusuf Fatthi", "Göz Hastalıkları", 37),
            new Doctor("Seda Korkmaz", "Göz Hastalıkları", 29),

            // Gynecology (Kadın Doğum)
            new Doctor("Ahmet Akkus", "Kadın Doğum", 42),
            new Doctor("Derya Uslu", "Kadın Doğum", 32),
            new Doctor("Nuran Polat", "Kadın Doğum", 58),

            // Dentistry (Diş)
            new Doctor("Selim Arslan", "Diş", 35),
            new Doctor("Nihat Bilgin", "Diş", 48),
            new Doctor("Recep Dogan", "Diş", 44),

            // Pediatrics (Çocuk)
            new Doctor("Dogukan Sasi", "Çocuk Hastalıkları", 31),
            new Doctor("Yusuf Sert", "Çocuk Hastalıkları", 40),
            new Doctor("Rabia Emirhan", "Çocuk Hastalıkları", 31)
        );

        // 3. Save all to MongoDB
        doctorRepo.saveAll(initialDoctors);
        System.out.println("🌱 Database seeded with " + initialDoctors.size() + " doctors!");
    }
}
