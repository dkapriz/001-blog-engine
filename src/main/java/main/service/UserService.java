package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.UserAdvancedDTO;
import main.api.request.AddUserRequest;
import main.api.request.LoginRequest;
import main.api.response.ResultResponse;
import main.api.response.UserResultResponse;
import main.config.BlogConfig;
import main.exception.IllegalParameterException;
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

import java.security.Principal;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

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
        user.setRegTime(Calendar.getInstance(TimeZone.getTimeZone(config.getTimeZone())));
        userRepository.save(user);
        BlogConfig.LOGGER.info(BlogConfig.MARKER_BLOG_INFO, "Регистрация пользователя - " + user.getName() +
                " - " + user.getEmail());
        return new ResultResponse(true);
    }

    private void checkEmailToDB(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            throw new IllegalParameterException(BlogConfig.ERROR_EMAIL_FRONTEND_NAME,
                    BlogConfig.ERROR_EMAIL_FRONTEND_MSG_REG);
        }
    }

    private void checkValidationEmail(String email) {
        String regexEmail = "\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*\\.\\w{2,4}";
        if (!email.matches(regexEmail)) {
            throw new IllegalParameterException(BlogConfig.ERROR_EMAIL_FRONTEND_NAME,
                    BlogConfig.ERROR_EMAIL_FRONTEND_MSG_FORMAT);
        }
    }

    private void checkValidationName(String name) {
        String regexName = "[A-Za-zА-Яа-я0-9\\s]{2,20}";
        if (!name.matches(regexName)) {
            throw new IllegalParameterException(BlogConfig.ERROR_NAME_FRONTEND_NAME,
                    BlogConfig.ERROR_NAME_FRONTEND_MSG);
        }
    }

    private void checkValidationPassword(String password) {
        if (password.length() < config.getUserMinLengthPassword()) {
            throw new IllegalParameterException(BlogConfig.ERROR_PASSWORD_FRONTEND_NAME,
                    BlogConfig.ERROR_PASSWORD_FRONTEND_MSG);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalParameterException("email",
                        "Пользователь с E-mail: " + email + " не зарегистрирован"));
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
