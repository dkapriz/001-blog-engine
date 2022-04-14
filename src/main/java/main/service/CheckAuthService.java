package main.service;

import main.api.dto.UserAuthDTO;
import main.api.response.CheckAuthResponse;
import org.springframework.stereotype.Service;

@Service
public class CheckAuthService {

    public CheckAuthResponse getResponse() {

        // TODO: Заглушка

        UserAuthDTO user = new UserAuthDTO(1, "Дмитрий",
                "https://i.ytimg.com/vi/E5cJVepFLRw/hqdefault_live.jpg",
                "dkapriz@mail.ru", true, 0, true);
        return new CheckAuthResponse(true, user);
    }
}