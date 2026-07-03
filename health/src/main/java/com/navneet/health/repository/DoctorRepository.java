package com.navneet.health.repository;

import com.navneet.health.entity.Doctor;
import com.navneet.health.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialization(String specialization);

    Optional<Doctor> findByUser(User user);
}