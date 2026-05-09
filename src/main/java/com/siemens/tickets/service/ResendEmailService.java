package com.siemens.tickets.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailService implements EmailService {

    private final Resend resend;

    public ResendEmailService(@Value("${resend.api.key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")
                    .to(to)
                    .subject(subject)
                    .text(body)
                    .build();
            resend.emails().send(params);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }
}