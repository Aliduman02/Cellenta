package com.resetSercice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@SpringBootApplication
@RestController
public class PasswordResetServiceApplication {

	@Autowired
	private JavaMailSender mailSender;

	public static void main(String[] args) {
		SpringApplication.run(PasswordResetServiceApplication.class, args);
	}

	@PostMapping("/send-reset-code")
	public ResponseEntity<Boolean> sendResetCode(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String code = body.get("code");

		if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
			return ResponseEntity.ok(false);
		}

		try {

			ZonedDateTime istanbulTime = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"));
			String now = istanbulTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));


			String htmlTemplate = """
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8" /><title>Şifre Sıfırlama</title></head>
<body style="margin:0; padding:0; font-family: Arial, sans-serif; background-color: #ffffff;">
    <div style="background:#fff; margin:0 auto; max-width:600px; border-radius:8px; border:2px solid #ccc;">
        <div style="text-align:center; padding:20px; background: linear-gradient(41deg, #16698f 0%, #00c7be 100%); border-radius:8px 8px 0 0;">
            <img src='cid:logo' alt='Cellenta Logo' width='200' style='display:block; margin:0 auto; padding:0; border:0;' />
        </div>
        <div style="padding:30px; color:#333;">
            <h2 style='color:#007BFF; font-weight:bold; margin-bottom:10px;'>Şifre Sıfırlama Talebi</h2>
            <p style="margin:0 0 10px 0;">Merhaba,</p>
            <p style="margin:0 0 10px 0;">Şifre sıfırlama kodunuz aşağıdadır:</p>
            <p style='font-size:24px; color:#d9534f; font-weight:bold; margin:20px 0;'>{{CODE}}</p>
            <p style="margin:0 0 10px 0;">Bu kod 2 dakika boyunca geçerlidir.</p>
            <hr style="margin: 30px 0;" />
            <div style="text-align:center; color:#888; font-size:12px;">
                <p style="margin:5px 0;">Bu e-posta {{DATE}} tarihinde oluşturulmuştur.</p>
                <p style="margin:5px 0;">Sorularınız için bizimle iletişime geçebilirsiniz.</p>
                <p style="margin:5px 0;">© 2025 Cellenta – Tüm hakları saklıdır.</p>
            </div>
        </div>
    </div>
</body>
</html>
""";

			String htmlContent = htmlTemplate
					.replace("{{CODE}}", code)
					.replace("{{DATE}}", now);

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(email);
			helper.setSubject("Şifre Sıfırlama Kodu");
			helper.setText(htmlContent, true);

			ClassPathResource logo = new ClassPathResource("static/images/cellenta-logo.png");
			helper.addInline("logo", logo);

			mailSender.send(message);

			return ResponseEntity.ok(true);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok(false);
		}
	}
}
