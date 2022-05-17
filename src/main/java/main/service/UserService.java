package main.service;

import lombok.AllArgsConstructor;
import main.api.dto.UserAdvancedDTO;
import main.api.dto.UserResultDTO;
import main.api.request.AddUserRequest;
import main.api.request.LoginRequest;
import main.api.response.ResultResponse;
import main.configuratoin.BlogConfig;
import main.exception.IllegalParameterException;
import main.model.User;
import main.model.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

@Service
@AllArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final CaptchaService captchaService;
    @Autowired
    private final BlogConfig config;

    public ResultResponse login(LoginRequest loginRequest) {

        // TODO: Заглушка

        return new UserResultDTO(true, new UserAdvancedDTO(1, "Дмитрий",
                "https://i.ytimg.com/vi/E5cJVepFLRw/hqdefault_live.jpg",
                "dkapriz@mail.ru", true, 0, true));
    }

    public ResultResponse checkAuth() {

        // TODO: Заглушка

        //      return new UserResultDTO(true, new UserAdvancedDTO(1, "Дмитрий",
        //             "https://i.ytimg.com/vi/E5cJVepFLRw/hqdefault_live.jpg",
        //              "dkapriz@mail.ru", true, 0, true));
        return new ResultResponse(false);
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
        user.setPassword(addUserRequest.getPassword());
        user.setRegTime(Calendar.getInstance(TimeZone.getTimeZone(config.getTimeZone())));
        userRepository.save(user);

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
}
