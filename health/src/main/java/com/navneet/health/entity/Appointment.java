package com.navneet.health.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="patient_id")
    private User patient;
    @ManyToOne
    @JoinColumn(name="doctor_id")
    private Doctor doctor;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(columnDefinition = "TEXT")
    private String preVisitSummary;

    @Column(columnDefinition = "TEXT")
    private String postVisitSummary;
    private String calendarEventId;
}
