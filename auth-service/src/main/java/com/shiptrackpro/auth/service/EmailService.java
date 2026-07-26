package com.shiptrackpro.auth.service;

/**
 * Email service contract for authentication emails.
 */
public interface EmailService {

    void sendVerificationEmail(String to, String name, String token);

    void sendPasswordResetEmail(String to, String name, String token);
}
