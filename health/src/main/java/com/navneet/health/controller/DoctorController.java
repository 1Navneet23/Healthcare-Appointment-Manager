package com.navneet.health.controller;
import com.navneet.health.service.DoctorService;
import com.navneet.health.entity.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
    @PostMapping("/admin/doctors/{id}/leave")
    public ResponseEntity<?> addLeaveDay(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            return ResponseEntity.ok(doctorService.addLeaveDay(id, java.time.LocalDate.parse(body.get("date"))));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/admin/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        try {
            return ResponseEntity.ok(doctorService.createDoctor(doctor));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Doctor profile already exists.");
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialization(
            @RequestParam String specialization) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
    }

    @GetMapping("/doctors/{id}/slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorService.getAvailableSlots(id, date));
    }
}
