package com.navneet.health.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id",unique = true, nullable = false)
    private User user;
    private String specialization;
    private String workingHoursStart;
    private String workingHoursEnd;
    private Integer slotDurationMinutes;
    @ElementCollection
    private List<LocalDate> leaveDays=new ArrayList<>();
}
