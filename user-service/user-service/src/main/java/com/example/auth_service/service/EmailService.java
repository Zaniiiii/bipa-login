package com.example.auth_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.example.auth_service.entity.User;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(User user, String token) {
        String subject = "Verifikasi Email";
        String confirmationUrl = "http://ec2-35-85-179-20.us-west-2.compute.amazonaws.com/gateway/api/auth/verify?token=" + token;
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
}