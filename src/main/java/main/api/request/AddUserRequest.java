package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddUserRequest {
    @JsonProperty("e_mail")
    private String email;

    private String password;
    private String name;
    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}