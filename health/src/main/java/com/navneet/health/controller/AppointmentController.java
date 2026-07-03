package com.navneet.health.controller;

import com.navneet.health.entity.Appointment;
import com.navneet.health.entity.Prescription;
import com.navneet.health.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment appointment) {
        try {
            return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/{id}/prescription")
    public ResponseEntity<?> addPrescription(@PathVariable Long id,
                                             @RequestBody Prescription prescription) {
        try {
            return ResponseEntity.ok(appointmentService.addPrescription(id, prescription));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getPatientAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsForDoctor(doctorId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.cancelAppointment(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/notes")
    public ResponseEntity<?> submitNotes(@PathVariable Long id,
                                         @RequestParam String notes) {
        try {
            return ResponseEntity.ok(appointmentService.submitDoctorNotes(id, notes));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}