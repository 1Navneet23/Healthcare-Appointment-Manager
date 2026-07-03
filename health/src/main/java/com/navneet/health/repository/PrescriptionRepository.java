package com.navneet.health.repository;

import com.navneet.health.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByAppointmentId(Long appointmentId);

}