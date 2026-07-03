package com.navneet.health.service;

import com.navneet.health.entity.*;
import com.navneet.health.repository.AppointmentRepository;
import com.navneet.health.repository.DoctorRepository;
import com.navneet.health.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    public Doctor addLeaveDay(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (!doctor.getLeaveDays().contains(date)) {
            doctor.getLeaveDays().add(date);
        }

        // Notify affected patients
        List<Appointment> affected = appointmentRepository
                .findByDoctorAndAppointmentDate(doctor, date);

        for (Appointment apt : affected) {
            if (apt.getStatus() == AppointmentStatus.CONFIRMED) {
                emailService.sendCancellationEmail(
                        apt.getPatient().getEmail(),
                        apt.getPatient().getName(),
                        doctor.getUser().getName(),
                        date.toString(),
                        apt.getAppointmentTime().toString()
                );
                apt.setStatus(AppointmentStatus.CANCELLED);
                appointmentRepository.save(apt);
            }
        }

        return doctorRepository.save(doctor);
    }
    public Doctor createDoctor(Doctor doctor) {

        if (doctor.getUser() == null || doctor.getUser().getId() == null) {
            throw new RuntimeException("User ID is required");
        }

        User user = userRepository.findById(doctor.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.DOCTOR) {
            throw new RuntimeException("Selected user is not a doctor.");
        }

        // Prevent duplicate doctor profile
        if (doctorRepository.findByUser(user).isPresent()) {
            throw new RuntimeException("Doctor profile already exists.");
        }

        doctor.setUser(user);

        return doctorRepository.save(doctor);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (doctor.getLeaveDays().contains(date)) {
            return new ArrayList<>();
        }

        List<Appointment> booked =
                appointmentRepository.findByDoctorAndAppointmentDate(doctor, date);

        List<LocalTime> bookedTimes =
                booked.stream().map(Appointment::getAppointmentTime).toList();

        List<LocalTime> allSlots = generateSlots(
                LocalTime.parse(doctor.getWorkingHoursStart()),
                LocalTime.parse(doctor.getWorkingHoursEnd()),
                doctor.getSlotDurationMinutes()
        );

        return allSlots.stream()
                .filter(slot -> !bookedTimes.contains(slot))
                .toList();
    }

    private List<LocalTime> generateSlots(LocalTime start,
                                          LocalTime end,
                                          int duration) {

        List<LocalTime> slots = new ArrayList<>();

        LocalTime current = start;

        while (current.isBefore(end)) {
            slots.add(current);
            current = current.plusMinutes(duration);
        }

        return slots;
    }
}