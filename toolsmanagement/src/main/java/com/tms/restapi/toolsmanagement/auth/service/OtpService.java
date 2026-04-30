package com.tms.restapi.toolsmanagement.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpEntry {
        String otp;
        long expiresAt;
        boolean verified;
        OtpEntry(String otp, long expiresAt) { this.otp = otp; this.expiresAt = expiresAt; this.verified = false; }
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final long TTL_SECONDS = 10 * 60; // 10 minutes

    private String key(String role, String email) {
        return role.trim().toUpperCase() + ":" + email.trim().toLowerCase();
    }

    public String generateOtp(String role, String email) {
        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);
        long expiresAt = Instant.now().getEpochSecond() + TTL_SECONDS;
        store.put(key(role, email), new OtpEntry(otp, expiresAt));
        return otp;
    }

    public boolean verifyOtp(String role, String email, String otp) {
        OtpEntry entry = store.get(key(role, email));
        if (entry == null) return false;
        long now = Instant.now().getEpochSecond();
        if (now > entry.expiresAt) {
            store.remove(key(role, email));
            return false;
        }
        if (entry.otp.equals(otp)) {
            entry.verified = true;
            return true;
        }
        return false;
    }

    public boolean isVerified(String role, String email) {
        OtpEntry entry = store.get(key(role, email));
        if (entry == null) return false;
        if (Instant.now().getEpochSecond() > entry.expiresAt) { store.remove(key(role, email)); return false; }
        return entry.verified;
    }

    public void clear(String role, String email) {
        store.remove(key(role, email));
    }
}
