package com.navneet.health.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GoogleCalendarConfig {

    @Value("${google.calendar.credentials.path}")
    private String credentialsPath;

    @Value("${google.calendar.tokens.path}")
    private String tokensPath;

    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);

    @Bean
    public Calendar googleCalendarService()
            throws GeneralSecurityException, IOException {

        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                jsonFactory, new java.io.InputStreamReader(
                        new org.springframework.core.io.ClassPathResource("credentials.json")
                                .getInputStream()));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(
                        new java.io.File(tokensPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(
                flow, receiver).authorize("user");

        return new Calendar.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Healthcare App")
                .build();
    }
}