package com.navneet.health.service;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CalenderService {

    private final Calendar googleCalendarService;

    public String createCalendarEvent(String patientEmail,
                                      String doctorEmail,
                                      String title,
                                      LocalDate date,
                                      LocalTime startTime,
                                      LocalTime endTime) {
        try {
            ZonedDateTime start = ZonedDateTime.of(date, startTime, ZoneId.systemDefault());
            ZonedDateTime end = ZonedDateTime.of(date, endTime, ZoneId.systemDefault());

            Event event = new Event()
                    .setSummary(title)
                    .setDescription("Healthcare appointment");

            event.setStart(new EventDateTime()
                    .setDateTime(new DateTime(start.toInstant().toEpochMilli()))
                    .setTimeZone(ZoneId.systemDefault().getId()));

            event.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(end.toInstant().toEpochMilli()))
                    .setTimeZone(ZoneId.systemDefault().getId()));

            event.setAttendees(Arrays.asList(
                    new EventAttendee().setEmail(patientEmail),
                    new EventAttendee().setEmail(doctorEmail)
            ));

            Event created = googleCalendarService.events()
                    .insert("primary", event)
                    .setSendUpdates("all")
                    .execute();

            return created.getId();

        } catch (Exception e) {
            System.err.println("Calendar event creation failed: " + e.getMessage());
            return null;
        }
    }

    public void deleteCalendarEvent(String eventId) {
        try {
            googleCalendarService.events().delete("primary", eventId).execute();
        } catch (Exception e) {
            System.err.println("Calendar deletion failed: " + e.getMessage());
        }
    }
}