package com.zynolo_nexus.auth_service.service.impl;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zynolo_nexus.auth_service.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${mail.from:no-reply@zynolonexus.com}")
    private String fromAddress;

    @Value("${spring.application.name:Zynolo NEXUS}")
    private String applicationName;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetOtp(String toEmail, String displayName, String otp) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("Skipping password reset email - missing destination address");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setFrom(fromAddress, applicationName + " Security");
            helper.setSubject("[" + applicationName + "] Password Reset Verification Code");
            helper.setText(buildOtpHtml(displayName, otp), true);
            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error("Failed to send password reset OTP email to {}", toEmail, ex);
        }
    }

    private String buildOtpHtml(String displayName, String otp) {
        String greeting = StringUtils.hasText(displayName) ? "Hello " + displayName + "," : "Hello,";
        return """
                <html>
                  <body style="font-family: 'Segoe UI', Arial, sans-serif; color:#202124;">
                    <div style="max-width:600px;margin:auto;border:1px solid #e0e0e0;border-radius:12px;padding:32px;">
                      <h2 style="color:#0d47a1;margin-top:0;">Zynolo NEXUS Account Security</h2>
                      <p>%s</p>
                      <p>Use the verification code below to reset your password. This code expires in 10 minutes.</p>
                      <div style="background:#f4f6fb;border-radius:8px;padding:16px;margin:24px 0;text-align:center;">
                        <span style="font-size:32px;letter-spacing:8px;font-weight:bold;color:#1a237e;">%s</span>
                      </div>
                      <p>If you did not request this, please ignore this email or contact the Zynolo NEXUS security team immediately.</p>
                      <p style="margin-top:32px;">Stay secure,<br/>Zynolo NEXUS Security Team</p>
                      <hr style="border:none;border-top:1px solid #e0e0e0;margin:32px 0;"/>
                      <p style="font-size:12px;color:#5f6368;">
                        This is an automated message from Zynolo NEXUS. Please do not reply directly to this email.
                      </p>
                    </div>
                  </body>
                </html>
                """.formatted(greeting, otp);
    }
}
