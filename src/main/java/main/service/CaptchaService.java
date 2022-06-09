package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import lombok.AllArgsConstructor;
import main.api.response.CaptchaResponse;
import main.config.BlogConfig;
import main.exception.ResultIllegalParameterException;
import main.model.CaptchaCode;
import main.model.repositories.CaptchaCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

@Service
@AllArgsConstructor
public class CaptchaService {
    @Autowired
    private final CaptchaCodeRepository captchaCodeRepository;
    @Autowired
    private final ImageService imageService;
    @Autowired
    private final BlogConfig config;
    @Autowired
    private final TimeService timeService;

    public CaptchaResponse generateCaptcha() {
        deleteExpiredCaptcha();

        Cage cage = new GCage();
        String token = cage.getTokenGenerator().next();
        BufferedImage scaledImage = imageService.resizeImage(cage.drawImage(token),
                config.getCaptchaHeight(), config.getCaptchaWight());
        String encodeImage = Base64.getEncoder().encodeToString(imageService
                .imageToByte(scaledImage, config.getCaptchaFormat()));
        String encodeImageURL = config.getCaptchaURL() + ", " + encodeImage;

        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setCode(token);
        captchaCode.setSecretCode(generateSecretCode());
        captchaCode.setTime(LocalDateTime.now());

        captchaCodeRepository.save(captchaCode);
        return new CaptchaResponse(captchaCode.getSecretCode(), encodeImageURL);
    }

    public void checkCaptchaCode(String secretCode, String code) {
        if (captchaCodeRepository.count() == 0) {
            return;
        }
        Iterable<CaptchaCode> captchaCodeIterable = captchaCodeRepository.findAll();
        boolean isFound = false;
        for (CaptchaCode captchaCode : captchaCodeIterable) {
            if (captchaCode.getSecretCode().equals(secretCode)) {
                if (captchaCode.getCode().equals(code)) {
                    isFound = true;
                }
            }
        }
        if (!isFound) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_CAPTCHA_FRONTEND_NAME,
                    BlogConfig.ERROR_CAPTCHA_FRONTEND_MSG);
        }
    }

    private void deleteExpiredCaptcha() {
        Iterable<CaptchaCode> captchaCodeIterable = captchaCodeRepository.findAll();
        LocalDateTime nowTimeMinusCaptchaTimeLive = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timeService.getNowTimestamp() - config.getCaptchaTimeLive()),
                ZoneId.systemDefault());
        for (CaptchaCode captchaCode : captchaCodeIterable) {
            if (captchaCode.getTime().isBefore(nowTimeMinusCaptchaTimeLive)) {
                captchaCodeRepository.delete(captchaCode);
            }
        }
    }

    private String generateSecretCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < BlogConfig.LENGTH_CAPTCHA_SECRET_CODE; i++) {
            sb.append(BlogConfig.SYMBOLS.charAt(random.nextInt(BlogConfig.SYMBOLS.length())));
        }
        return sb.toString();
    }
}
