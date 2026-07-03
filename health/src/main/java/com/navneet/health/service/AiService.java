package com.navneet.health.service;

import com.navneet.health.dto.groq.Choice;
import com.navneet.health.dto.groq.GroqRequest;
import com.navneet.health.dto.groq.GroqResponse;
import com.navneet.health.dto.groq.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestClient restClient =
            RestClient.builder().build();

    public String generatePreVisitSummary(String symptoms) {

        String prompt = """
                You are an experienced physician.

                Analyze these symptoms and provide:

                1. Urgency Level (Low/Medium/High)

                2. Chief Complaint

                3. Three questions the doctor should ask.

                Symptoms:

                """ + symptoms;

        return generate(prompt,
                "Pre-visit summary unavailable.");
    }

    public String generatePostVisitSummary(String doctorNotes) {

        String prompt = """
                You are a medical assistant.

                Convert these doctor's notes into
                patient-friendly language.

                Include:

                1. Diagnosis

                2. Medication Schedule

                3. Lifestyle Advice

                4. Follow-up Instructions

                Doctor Notes:

                """ + doctorNotes;

        return generate(prompt,
                "Post-visit summary unavailable.");
    }

    private String generate(String prompt,
                            String fallback) {

        try {
            return callGroq(prompt);
        } catch (RestClientException ex) {
            return fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private String callGroq(String prompt) {

        GroqRequest request = new GroqRequest(
                model,
                List.of(
                        new Message(
                                "user",
                                prompt
                        )
                ),
                1024
        );

        GroqResponse response =
                restClient.post()
                        .uri(apiUrl)
                        .header(
                                "Authorization",
                                "Bearer " + apiKey
                        )
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .body(request)
                        .retrieve()
                        .body(GroqResponse.class);

        if (response == null
                || response.getChoices() == null
                || response.getChoices().isEmpty()) {

            throw new RuntimeException(
                    "No response received from Groq."
            );
        }

        Choice choice =
                response.getChoices().get(0);

        return choice.getMessage().getContent();
    }

}