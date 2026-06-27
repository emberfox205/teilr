package frauas.teilr.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@teilr.local}")
    private String fromAddress;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private JavaMailSender mailSender;

    @PostConstruct
    void resolveSender() {
        this.mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("No JavaMailSender configured — confirmation links will be printed to the log instead of emailed.");
        }
    }

    /**
     * Send the verification link to a freshly registered user. If no SMTP is
     * configured the link is logged so development still works.
     */
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String link = baseUrl + "/auth/verify?token=" + token;
        String body = """
                Hi %s,

                Welcome to teilr! Please confirm your email by opening the link below:

                %s

                If you didn't sign up, you can safely ignore this message.
                """.formatted(username, link);

        if (mailSender == null) {
            log.info("[DEV] Verification link for {} ({}): {}", username, toEmail, link);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Confirm your teilr account");
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            log.info("[FALLBACK] Verification link for {}: {}", username, link);
        }
    }
}
