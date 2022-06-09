package main.service;

import lombok.AllArgsConstructor;
import main.config.BlogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@AllArgsConstructor
public class MailService {
    private final String ENCODING_OPTIONS = "text/html; charset=UTF-8";
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private BlogConfig config;

    public void send(String emailTo, String subject, String message) throws MessagingException {
        MimeMessage mailMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "UTF-8");
        helper.setFrom(config.getMailFromUserName());
        helper.setTo(emailTo);
        helper.setSubject(subject);
        mailMessage.setContent(message, ENCODING_OPTIONS);
        mailSender.send(mailMessage);
    }
}
