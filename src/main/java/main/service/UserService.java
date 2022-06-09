package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.UserAdvancedDTO;
import main.api.request.*;
import main.api.response.ResultResponse;
import main.api.response.UserResultResponse;
import main.config.BlogConfig;
import main.exception.ResultIllegalParameterException;
import main.model.Post;
import main.model.User;
import main.model.repositories.PostRepository;
import main.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private MailService mailService;
    @Autowired
    private BlogConfig config;
    @Autowired
    private AuthenticationManager authenticationManager;

    public ResultResponse login(LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        org.springframework.security.core.userdetails.User user =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        User currentUser = getUserByEmail(user.getUsername());
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Вход пользователя - " + currentUser.getName() +
                " - " + currentUser.getEmail());
        return new UserResultResponse(true, UserToUserAdvancedDTO(currentUser));
    }

    public ResultResponse logout() {
        SecurityContextHolder.clearContext();
        return new ResultResponse(true);
    }

    public ResultResponse checkAuth(Principal principal) {
        if (principal == null) {
            return new ResultResponse(false);
        }
        return new UserResultResponse(true, UserToUserAdvancedDTO(getLoggedUser()));
    }

    public ResultResponse registration(AddUserRequest addUserRequest) {
        checkValidationEmail(addUserRequest.getEmail());
        checkEmailToDB(addUserRequest.getEmail());
        checkValidationName(addUserRequest.getName());
        checkValidationPassword(addUserRequest.getPassword());
        captchaService.checkCaptchaCode(addUserRequest.getCaptchaSecret(), addUserRequest.getCaptcha());
        User user = new User();
        user.setName(addUserRequest.getName());
        user.setEmail(addUserRequest.getEmail());
        user.setPassword(encodePassword(addUserRequest.getPassword()));
        user.setRegTime(LocalDateTime.now());
        userRepository.save(user);
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Регистрация пользователя - " + user.getName() +
                " - " + user.getEmail());
        return new ResultResponse(true);
    }

    public ResultResponse editProfile(MultipartFile photo, ProfileRequest profileRequest) throws IOException {
        ResultResponse resultResponse = editProfile(profileRequest);
        User user = getLoggedUser();
        String avatarPath = imageService.SaveResizerImage(photo, String.valueOf(user.getId()));
        user.setPhoto(avatarPath);
        userRepository.save(user);
        return resultResponse;
    }

    public ResultResponse editProfile(ProfileRequest profileRequest) {
        User user = getLoggedUser();

        checkValidationName(profileRequest.getName());
        user.setName(profileRequest.getName());

        checkValidationEmail(profileRequest.getEmail());
        if (userRepository.existsByIdAndEmailIgnoreCase(user.getId(), profileRequest.getEmail()) ||
                !userRepository.existsByEmailIgnoreCase(profileRequest.getEmail())) {
            user.setEmail(profileRequest.getEmail());
        } else {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_EMAIL_FRONTEND_NAME,
                    BlogConfig.ERROR_EMAIL_FRONTEND_MSG_REG);
        }

        if (profileRequest.getPassword() != null) {
            checkValidationPassword(profileRequest.getPassword());
            user.setPassword(encodePassword(profileRequest.getPassword()));
        }

        if (profileRequest.getRemovePhoto() == config.getImageAvatarRemoveValue()) {
            imageService.removeFile(user.getPhoto());
            user.setPhoto("");
        }

        userRepository.save(user);
        return new ResultResponse(true);
    }

    public ResultResponse restorePassword(PassRestoreRequest recoveryRequest) throws MessagingException {
        if (!userRepository.existsByEmailIgnoreCase(recoveryRequest.getEmail())) {
            BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Попытка восстановления пароль. Email: " +
                    recoveryRequest.getEmail() + " не найден в базе");
            return new ResultResponse(false);
        }
        User user = getUserByEmail(recoveryRequest.getEmail());
        String hash = UUID.randomUUID().toString();
        String link = config.getMailDomainName() + config.getMailChangePasswordSubAddress() + hash;
        String msg = config.getMailRestorePasswordMsgPartBeforeLink() + " <a href=" + link + ">ссылке</a>"
                + config.getMailRestorePasswordMsgPartAfterLink();

        user.setCode(hash);
        userRepository.save(user);
        mailService.send(recoveryRequest.getEmail(), config.getMailRestorePasswordSubject(), msg);
        return new ResultResponse(true);
    }

    public ResultResponse recoveryPassword(PassRecoveryRequest passRecoveryRequest) {
        User user = getUserByRecoveryCode(passRecoveryRequest.getCode());
        captchaService.checkCaptchaCode(passRecoveryRequest.getCaptchaSecret(), passRecoveryRequest.getCaptcha());
        checkValidationPassword(passRecoveryRequest.getPassword());
        user.setPassword(encodePassword(passRecoveryRequest.getPassword()));
        user.setCode(null);
        userRepository.save(user);
        return new ResultResponse(true);
    }

    private void checkEmailToDB(String email) {
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);
        if (user.isPresent()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_EMAIL_FRONTEND_NAME,
                    BlogConfig.ERROR_EMAIL_FRONTEND_MSG_REG);
        }
    }

    private void checkValidationEmail(String email) {
        String regexEmail = "\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*\\.\\w{2,4}";
        if (!email.matches(regexEmail)) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_EMAIL_FRONTEND_NAME,
                    BlogConfig.ERROR_EMAIL_FRONTEND_MSG_FORMAT);
        }
    }

    private void checkValidationName(String name) {
        String regexName = "[A-Za-zА-Яа-я0-9\\s]{2,20}";
        if (!name.matches(regexName)) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_NAME_FRONTEND_NAME,
                    BlogConfig.ERROR_NAME_FRONTEND_MSG);
        }
    }

    private void checkValidationPassword(String password) {
        if (password.length() < config.getUserMinLengthPassword()) {
            throw new ResultIllegalParameterException(BlogConfig.ERROR_PASSWORD_FRONTEND_NAME,
                    BlogConfig.ERROR_PASSWORD_FRONTEND_MSG);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResultIllegalParameterException("email",
                        "Пользователь с E-mail: " + email + " не зарегистрирован"));
    }

    private User getUserByRecoveryCode(String code) {
        return userRepository.findByCode(code).orElseThrow(() -> new ResultIllegalParameterException(
                BlogConfig.ERROR_CODE_FRONTEND_NAME,
                BlogConfig.ERROR_LINK_IS_OUTDATED_BEFORE + config.getMailRestorePasswordSubAddress() +
                        BlogConfig.ERROR_LINK_IS_OUTDATED_AFTER));
    }

    public UserAdvancedDTO UserToUserAdvancedDTO(User user) {
        return new UserAdvancedDTO(
                user.getId(),
                user.getName(),
                user.getPhoto(),
                user.getEmail(),
                isModerator(user),
                postRepository.countAllActiveAndUnModeration().orElse(0),
                isModerator(user));
    }

    public boolean isModerator(User user) {
        return user.getIsModerator() == BlogConfig.USER_MODERATOR;
    }

    public boolean isAuthor(User user, Post post) {
        return user.getId() == post.getUser().getId();
    }

    public User getLoggedUser() {
        String loggedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(loggedUserEmail);
    }

    private String encodePassword(String password) {
        return new BCryptPasswordEncoder(BlogConfig.B_CRYPT_STRENGTH).encode(password);
    }
}
