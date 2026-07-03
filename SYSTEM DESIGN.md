# System Design Write-up
## Healthcare Appointment & Follow-up Manager

---

## 1. Double-Booking Prevention

Double-booking is the most critical concurrency problem in any appointment system. The naive approach — checking whether a slot is already taken before saving — fails under concurrent load. If two patients send booking requests for the same slot at the same millisecond, both requests read "slot is free" before either one writes, and both proceed to book. The result is two confirmed appointments for the same doctor at the same time.

This system uses a two-layer defense.

The first layer is an application-level check in `AppointmentService.bookAppointment()`. Before attempting to save, the system queries the database for any existing appointment with the same doctor, date, and time that is not cancelled. If one exists, the request is rejected immediately with a clear error message. This handles the common case efficiently and gives users a fast, readable response.

The second layer is a PostgreSQL partial unique index:

```sql
CREATE UNIQUE INDEX uq_doctor_slot_active
ON appointment (doctor_id, appointment_date, appointment_time)
WHERE status <> 'CANCELLED';
```

This index enforces uniqueness at the database level, but only for non-cancelled appointments. The `WHERE status <> 'CANCELLED'` clause is deliberate — it means a slot that was previously booked and cancelled can be rebooked freely. Without this partial condition, cancelled slots would remain permanently blocked.

The method uses `saveAndFlush()` instead of `save()` to force the insert to happen immediately within the transaction. Any concurrent request that slips past the application check will be rejected by the database with a `DataIntegrityViolationException`, which is caught and converted into a user-friendly error. The `@Transactional` annotation on the method ensures the entire operation — check, save, flush — is atomic.

This two-layer approach means the application check handles 99.9% of cases quickly, while the database constraint is the true safety net for race conditions.

---

## 2. Doctor Leave Conflict Handling

When an admin marks a doctor on leave for a specific date, the system cannot simply block future bookings. It must also handle existing confirmed appointments on that date and notify affected patients immediately.

The flow in `DoctorService.addLeaveDay()` is:

First, the leave date is added to the doctor's `leaveDays` list, which is stored as a separate `doctor_leave_days` table via JPA's `@ElementCollection`. This persists the leave day permanently.

Second, the system immediately queries all confirmed appointments for that doctor on that date using `AppointmentRepository.findByDoctorAndAppointmentDate()`. For each affected appointment, two things happen: the appointment status is set to `CANCELLED`, and a cancellation email is sent to the patient via the Brevo email service. The email explicitly states the reason — the doctor is unavailable — and asks the patient to rebook.

Each email send is wrapped in its own try/catch block. If an email fails for one patient, the system continues processing the remaining affected appointments rather than stopping. This prevents a single email failure from leaving other patients uninformed.

Future booking attempts on the leave date are rejected at the slot generation stage — `DoctorService.getAvailableSlots()` checks `doctor.getLeaveDays().contains(date)` and returns an empty list if the doctor is on leave, so the patient never even sees slots to pick from.

---

## 3. Slot Hold Mechanism

A slot hold mechanism temporarily reserves a slot between the moment a patient selects it and the moment they confirm the booking. This prevents the slot from being taken by another patient during the time the first patient is filling in the symptom form.

This system uses an optimistic approach — no hold is placed. The slot remains available until a booking is actually confirmed. The reasoning is practical: the symptom form is short and typically completed in under two minutes. Implementing a hold would require a separate `held_slots` table, a background job to release expired holds, and additional complexity in both the booking flow and the slot generation logic.

The trade-off is that in rare cases, two patients could simultaneously select the same slot and one would receive a "slot already booked" error after filling the symptom form. This is handled gracefully — the error message is clear and the patient is returned to slot selection. In a production system with high concurrent load, a Redis-based distributed lock with a 3-minute TTL would be the appropriate solution. For this system's expected load, the optimistic approach is sufficient and significantly simpler.

---

## 4. Notification Failure Handling

The system treats notifications as secondary to the core booking operation. A failed email or calendar event must never cause a booking to fail or roll back.

Every notification call — patient confirmation email, doctor notification email, Google Calendar event creation — is wrapped in its own independent try/catch block inside `AppointmentService.bookAppointment()`. The structure is sequential but isolated: if the patient email fails, the code still attempts the doctor email. If the calendar event creation fails, the appointment remains confirmed. Failures are logged to `System.err` for debugging but do not surface to the user.

The same pattern applies to cancellations — the cancellation email and calendar event deletion are both wrapped independently.

For the background reminder job in `ReminderService`, the `@Scheduled(cron = "0 0 8 * * *")` method iterates over all confirmed appointments for the next day and sends a reminder email to each patient. Each email is individually try/caught so a failure for one patient does not stop reminders for others.

For medication reminders, the scheduler also iterates over active prescriptions and sends medication reminders based on the `frequencyPerDay` field in the `Prescription` entity. These are also individually isolated — a failed reminder for one prescription does not affect others.

The Google Calendar integration uses OAuth 2.0 with offline access, meaning refresh tokens are stored in the `tokens/` directory and automatically used to obtain new access tokens when they expire. Calendar failures are caught gracefully — if the Google API is unavailable, the `calendarEventId` field on the appointment remains null, and the booking still completes successfully.

This design reflects a deliberate architectural decision: the appointment booking is the critical operation, and all notifications are best-effort secondary operations that should never block or break the primary flow.
