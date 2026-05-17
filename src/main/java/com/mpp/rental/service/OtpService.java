package com.mpp.rental.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${otp.expiry.minutes:10}")
    private int otpExpiryMinutes;

    // In-memory OTP store: email -> OtpEntry
    // ConcurrentHashMap is thread-safe for concurrent requests
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final SecureRandom RANDOM = new SecureRandom();

    // Tracks emails that have completed OTP verification but not yet registered
    private final Map<String, LocalDateTime> verifiedEmails = new ConcurrentHashMap<>();

    // ── Inner record to hold OTP + expiry ────────────────────────────────────
    private record OtpEntry(String otp, LocalDateTime expiresAt) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    // ==================== GENERATE & SEND OTP ====================

    /**
     * Generate a 6-digit OTP, store it, and send it to the given email.
     * If an OTP was already sent to this email, it gets overwritten with a fresh one.
     */
    public void sendOtp(String email) {
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        otpStore.put(email.toLowerCase(), new OtpEntry(otp, expiresAt));

        log.info("OTP generated for {}: {} (expires at {})", email, otp, expiresAt);

        sendOtpEmail(email, otp);
    }

    // ==================== VERIFY OTP ====================

    /**
     * Verify the OTP entered by the user.
     * Returns true if correct and not expired.
     * Removes the OTP from store on successful verification (one-time use).
     */
    public boolean verifyOtp(String email, String enteredOtp) {
        OtpEntry entry = otpStore.get(email.toLowerCase());

        if (entry == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }

        if (entry.isExpired()) {
            otpStore.remove(email.toLowerCase());
            log.warn("OTP expired for email: {}", email);
            return false;
        }

        if (!entry.otp().equals(enteredOtp.trim())) {
            log.warn("Incorrect OTP for email: {}", email);
            return false;
        }

        // Valid — remove so it can't be reused
        otpStore.remove(email.toLowerCase());
        log.info("OTP verified successfully for: {}", email);
        return true;
    }

    // ==================== CHECK IF OTP EXISTS ====================

    /**
     * Check whether a pending (non-expired) OTP exists for an email.
     * Used by the frontend to show the correct UI state on page refresh.
     */
    public boolean hasActiveOtp(String email) {
        OtpEntry entry = otpStore.get(email.toLowerCase());
        return entry != null && !entry.isExpired();
    }

    // ==================== MARK / CHECK VERIFIED ====================

    /**
     * Called after successful OTP verification.
     * Marks email as verified so registerUser() can proceed.
     * Entry expires after 30 minutes — user must re-verify if they wait too long.
     */
    public void markVerified(String email) {
        verifiedEmails.put(email.toLowerCase(), LocalDateTime.now().plusMinutes(30));
    }

    /**
     * Check if the email has been OTP-verified and the verification hasn't expired.
     * Called by UserService before creating the account.
     */
    public boolean isVerified(String email) {
        LocalDateTime expiry = verifiedEmails.get(email.toLowerCase());
        if (expiry == null) return false;
        if (LocalDateTime.now().isAfter(expiry)) {
            verifiedEmails.remove(email.toLowerCase());
            return false;
        }
        return true;
    }

    /**
     * Clear verification after successful registration so it can't be reused.
     */
    public void clearVerification(String email) {
        verifiedEmails.remove(email.toLowerCase());
    }

    // ==================== PRIVATE HELPERS ====================

    private String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000); // always 6 digits
        return String.valueOf(code);
    }

    private void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "MPP Business Rental");
            helper.setTo(toEmail);
            helper.setSubject("Your Verification Code — MPP Business Rental");
            helper.setText(buildEmailHtml(otp), true); // true = HTML

            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email. Please try again.");
        }
    }

    private String buildEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background: #f9fafb; padding: 40px 20px;">
                  <div style="max-width: 480px; margin: 0 auto; background: white;
                              border-radius: 12px; border: 1px solid #e5e7eb; padding: 40px;">
                    <h2 style="color: #7c3aed; margin-top: 0;">MPP Business Rental</h2>
                    <p style="color: #374151; font-size: 15px;">
                      Your email verification code is:
                    </p>
                    <div style="background: #f3f4f6; border-radius: 8px; padding: 20px;
                                text-align: center; margin: 24px 0;">
                      <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                   color: #7c3aed;">%s</span>
                    </div>
                    <p style="color: #6b7280; font-size: 13px;">
                      This code expires in <strong>10 minutes</strong>.
                      Do not share this code with anyone.
                    </p>
                    <p style="color: #6b7280; font-size: 13px;">
                      If you did not request this, please ignore this email.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(otp);
    }
}