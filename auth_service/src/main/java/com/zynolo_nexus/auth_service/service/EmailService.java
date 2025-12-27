package com.zynolo_nexus.auth_service.service;

public interface EmailService {

    void sendPasswordResetOtp(String toEmail, String displayName, String otp);
}
