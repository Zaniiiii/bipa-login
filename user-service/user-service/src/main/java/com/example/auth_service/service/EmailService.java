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
        String subject = "Verifikasi Pos-el";
        String confirmationUrl = verifyUrl  + token;
        String message = "<html>" +
                "<body>" +
                "<h3>Halo, " + user.getUsername() + "</h3>" +
                "<p>Terima kasih telah mendaftar. Silakan klik tautan di bawah ini untuk memverifikasi alamat pos-el Anda:</p>" +
                "<p><a href=\"" + confirmationUrl + "\">Verifikasi pos-el Saya</a></p>" +
                "<p>Jika Anda tidak mendaftar akun ini, abaikan pos-el ini.</p>" +
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
            throw new RuntimeException("Gagal mengirim pos-el", e);
        }
    }

    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Mengganti Kata Sandi";
        // Gunakan URL frontend dengan token sebagai parameter
        String resetUrl = frontendResetPasswordUrl + "?token=" + token;
        String message = "<html>" +
                "<body>" +
                "<h3>Halo, " + user.getUsername() + "</h3>" +
                "<p>Anda telah meminta untuk mengganti kata sandi Anda. Silakan klik tautan di bawah ini untuk mengatur ulang kata sandi anda Anda:</p>" +
                "<p><a href=\"" + resetUrl + "\">Ganti kata sandi Saya</a></p>" +
                "<p>Jika Anda tidak meminta mengganti kata sandi, abaikan pos-el ini.</p>" +
                "<br>" +
                "</body>" +
                "</html>";

        sendHtmlEmail(user.getEmail(), subject, message);
    }
}