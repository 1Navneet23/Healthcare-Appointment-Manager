package com.navneet.health.repository;
import com.navneet.health.entity.Appointment;
import com.navneet.health.entity.AppointmentStatus;
import com.navneet.health.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDate date);
    Optional<Appointment> findByDoctorAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Doctor doctor, LocalDate date, LocalTime time, AppointmentStatus status
    );
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);
    List<Appointment> findByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);
}
