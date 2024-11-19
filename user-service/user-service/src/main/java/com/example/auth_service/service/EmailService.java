package com.example.auth_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.example.auth_service.entity.User;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${VERIFY_URL}")
    private String verifyUrl;
    @Value("${FRONTEND_RESET_PASSWORD_URL}")
    private String frontendResetPasswordUrl;

    public void sendVerificationEmail(User user, String token) {
        String subject = "Verifikasi Email";
        String confirmationUrl = verifyUrl  + token;
        String message = "<html>" +
                "<body>" +
                "<h3>Halo, " + user.getUsername() + "</h3>" +
                "<p>Terima kasih telah mendaftar. Silakan klik tautan di bawah ini untuk memverifikasi alamat email Anda:</p>" +
                "<p><a href=\"" + confirmationUrl + "\">Verifikasi Email Saya</a></p>" +
                "<p>Jika Anda tidak mendaftar akun ini, abaikan email ini.</p>" +
                "<br>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(user.getEmail(), subject, message);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // Mengatur konten email dalam format HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Gagal mengirim email", e);
        }
    }

    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Reset Password";
        // Gunakan URL frontend dengan token sebagai parameter
        String resetUrl = frontendResetPasswordUrl + "?token=" + token;
        String message = "<html>" +
                "<body>" +
                "<h3>Halo, " + user.getUsername() + "</h3>" +
                "<p>Anda telah meminta untuk mereset password Anda. Silakan klik tautan di bawah ini untuk mengatur ulang password Anda:</p>" +
                "<p><a href=\"" + resetUrl + "\">Reset Password Saya</a></p>" +
                "<p>Jika Anda tidak meminta reset password ini, abaikan email ini.</p>" +
                "<br>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(user.getEmail(), subject, message);
    }
}