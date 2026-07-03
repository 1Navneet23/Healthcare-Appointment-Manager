package com.navneet.health.service;

import com.navneet.health.entity.Appointment;
import com.navneet.health.entity.AppointmentStatus;
import com.navneet.health.entity.Prescription;
import com.navneet.health.repository.AppointmentRepository;
import com.navneet.health.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final PrescriptionRepository prescriptionRepository;
    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendAppointmentReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Get all confirmed appointments for tomorrow
        List<Appointment> tomorrowAppointments = appointmentRepository
                .findByAppointmentDateAndStatus(tomorrow, AppointmentStatus.CONFIRMED);

        for (Appointment appointment : tomorrowAppointments) {
            try {
                emailService.sendReminderEmail(
                        appointment.getPatient().getEmail(),
                        appointment.getPatient().getName(),
                        appointment.getDoctor().getUser().getName(),
                        appointment.getAppointmentDate().toString(),
                        appointment.getAppointmentTime().toString()
                );
            } catch (Exception e) {
                // Log and continue to next appointment
                System.err.println("Reminder failed for appointment " + appointment.getId());
            }
        }
    }
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMedicationReminders() {
        List<Prescription> allPrescriptions = prescriptionRepository.findAll();
        for (Prescription p : allPrescriptions) {
            try {
                String patientEmail = p.getAppointment().getPatient().getEmail();
                String patientName = p.getAppointment().getPatient().getName();
                String subject = "Medication Reminder: " + p.getMedicationName();
                String body = "Dear " + patientName + ",\n\nReminder to take your medication:\n"
                        + p.getMedicationName() + " - " + p.getFrequencyPerDay()
                        + " time(s) per day\nInstructions: " + p.getInstructions();
                emailService.sendEmail(patientEmail, subject, body);
            } catch (Exception e) {
                System.err.println("Medication reminder failed: " + e.getMessage());
            }
        }
    }
}