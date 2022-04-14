package main.service;

import main.api.dto.UserAuthDTO;
import main.api.request.LoginRequest;
import main.api.response.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    public LoginResponse getResponse(LoginRequest loginRequest) {

        // TODO: Заглушка

        UserAuthDTO user = new UserAuthDTO(1, "Дмитрий",
                "https://i.ytimg.com/vi/E5cJVepFLRw/hqdefault_live.jpg",
                "dkapriz@mail.ru", true, 0, true);
        return new LoginResponse(true, user);
    }
}
