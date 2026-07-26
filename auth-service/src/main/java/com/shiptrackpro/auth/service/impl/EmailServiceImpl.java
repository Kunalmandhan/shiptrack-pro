package com.shiptrackpro.auth.service.impl;

import com.shiptrackpro.auth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using Spring Mail (JavaMailSender).
 * All email sending is @Async to avoid blocking the auth flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@shiptrackpro.com}")
    private String fromEmail;

    @Override
    @Async
    public void sendVerificationEmail(String to, String name, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;

        String subject = "ShipTrack Pro — Verify Your Email";
        String body = buildEmailTemplate(
                name,
                "Welcome to ShipTrack Pro!",
                "Please verify your email address to activate your account.",
                "Verify Email",
                verifyUrl,
                "This link expires in 24 hours."
        );

        sendHtmlEmail(to, subject, body);
        log.info("Verification email sent to: {}", to);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        String subject = "ShipTrack Pro — Reset Your Password";
        String body = buildEmailTemplate(
                name,
                "Password Reset Request",
                "We received a request to reset your password. Click the button below to choose a new password.",
                "Reset Password",
                resetUrl,
                "This link expires in 60 minutes. If you didn't request this, please ignore this email."
        );

        sendHtmlEmail(to, subject, body);
        log.info("Password reset email sent to: {}", to);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {} | Subject: {} | Error: {}", to, subject, e.getMessage());
        }
    }

    /**
     * Build a clean, responsive HTML email template.
     */
    private String buildEmailTemplate(String name, String heading, String message,
                                       String buttonText, String buttonUrl, String footer) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0; padding:0; font-family:'Inter',Arial,sans-serif; background-color:#0f172a;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px; margin:40px auto;">
                        <tr>
                            <td style="background:#1e293b; border-radius:16px; padding:40px; border:1px solid rgba(255,255,255,0.05);">
                                <!-- Logo -->
                                <div style="text-align:center; margin-bottom:32px;">
                                    <span style="font-size:24px; font-weight:800; background:linear-gradient(135deg,#818cf8,#34d399); -webkit-background-clip:text; -webkit-text-fill-color:transparent;">
                                        ShipTrack Pro
                                    </span>
                                </div>

                                <!-- Content -->
                                <h1 style="color:#f1f5f9; font-size:22px; margin:0 0 12px 0;">%s</h1>
                                <p style="color:#94a3b8; font-size:15px; line-height:1.6; margin:0 0 8px 0;">Hi %s,</p>
                                <p style="color:#94a3b8; font-size:15px; line-height:1.6; margin:0 0 32px 0;">%s</p>

                                <!-- CTA Button -->
                                <div style="text-align:center; margin:32px 0;">
                                    <a href="%s" style="display:inline-block; background:linear-gradient(135deg,#6366f1,#4f46e5); color:#ffffff; text-decoration:none; padding:14px 32px; border-radius:10px; font-weight:600; font-size:15px;">
                                        %s
                                    </a>
                                </div>

                                <!-- Footer -->
                                <p style="color:#64748b; font-size:13px; line-height:1.5; margin:24px 0 0 0; padding-top:24px; border-top:1px solid rgba(255,255,255,0.05);">
                                    %s
                                </p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(heading, name, message, buttonUrl, buttonText, footer);
    }
}
