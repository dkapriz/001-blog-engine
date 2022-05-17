package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.AddUserRequest;
import main.api.request.LoginRequest;
import main.api.response.CaptchaResponse;
import main.api.response.ResultResponse;
import main.service.CaptchaService;
import main.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController {

    private final CaptchaService captchaService;
    private final UserService userService;

    @GetMapping("/check")
    private ResponseEntity<ResultResponse> checkAuth() {
        return new ResponseEntity<>(userService.checkAuth(), HttpStatus.OK);
    }

    @GetMapping("/captcha")
    private ResponseEntity<CaptchaResponse> captcha(){
        return new ResponseEntity<>(captchaService.generateCaptcha(), HttpStatus.OK);
    }

    @PostMapping("/register")
    private ResponseEntity<ResultResponse> register(@RequestBody AddUserRequest addUserRequest) {
        return new ResponseEntity<>(userService.registration(addUserRequest), HttpStatus.OK);
    }

    @PostMapping("/login")
    private ResponseEntity<ResultResponse> login(@RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(userService.login(loginRequest), HttpStatus.OK);
    }
}