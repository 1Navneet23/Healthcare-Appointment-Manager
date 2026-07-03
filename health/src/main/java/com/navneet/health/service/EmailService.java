package com.navneet.health.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String toEmail,
                          String subject,
                          String body) {

        try {

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.set("api-key", apiKey);

            Map<String, Object> request = Map.of(

                    "sender", Map.of(
                            "name", fromName,
                            "email", fromEmail
                    ),

                    "to", List.of(
                            Map.of("email", toEmail)
                    ),

                    "subject", subject,

                    "htmlContent",
                    "<html><body><pre>"
                            + body
                            + "</pre></body></html>"
            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    entity,
                    String.class
            );

        } catch (Exception e) {

            System.err.println("Email sending failed: "
                    + e.getMessage());

        }

    }

    public void sendBookingConfirmation(
            String toEmail,
            String patientName,
            String doctorName,
            String date,
            String time) {

        sendEmail(
                toEmail,
                "Appointment Confirmed",

                "Dear "
                        + patientName
                        + ",\n\n"

                        + "Your appointment with Dr. "
                        + doctorName
                        + " has been confirmed.\n\n"

                        + "Date: "
                        + date
                        + "\n"

                        + "Time: "
                        + time
                        + "\n\n"

                        + "Please arrive 10 minutes early."
        );
    }

    public void sendCancellationEmail(
            String toEmail,
            String patientName,
            String doctorName,
            String date,
            String time) {

        sendEmail(
                toEmail,
                "Appointment Cancelled",

                "Dear "
                        + patientName
                        + ",\n\n"

                        + "Your appointment with Dr. "
                        + doctorName
                        + " on "
                        + date
                        + " at "
                        + time
                        + " has been cancelled."
        );
    }

    public void sendReminderEmail(
            String toEmail,
            String patientName,
            String doctorName,
            String date,
            String time) {

        sendEmail(
                toEmail,
                "Appointment Reminder",

                "Reminder:\n\n"

                        + "Appointment with Dr. "
                        + doctorName
                        + "\n"

                        + date
                        + " "
                        + time
        );
    }

    public void sendDoctorNotification(
            String toEmail,
            String doctorName,
            String patientName,
            String date,
            String time) {

        sendEmail(
                toEmail,
                "New Appointment",

                "Hello Dr. "
                        + doctorName
                        + ",\n\n"

                        + patientName
                        + " booked an appointment.\n"

                        + date
                        + " "
                        + time
        );
    }

}