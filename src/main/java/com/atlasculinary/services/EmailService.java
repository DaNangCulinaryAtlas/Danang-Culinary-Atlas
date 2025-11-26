package com.atlasculinary.services;

public interface EmailService {
    /**
     * Send an email to a recipient
     * @param to Recipient email address
     * @param subject Email subject
     * @param content Email content (can be HTML)
     */
    void sendEmail(String to, String subject, String content);
}
