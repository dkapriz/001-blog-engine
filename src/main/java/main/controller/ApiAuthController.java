package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.LoginRequest;
import main.api.response.CheckAuthResponse;
import main.api.response.LoginResponse;
import main.service.CheckAuthService;
import main.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController {

    private final CheckAuthService checkAuthService;
    private final LoginService loginService;

    @GetMapping("/check")
    private ResponseEntity<CheckAuthResponse> checkAuth() {
        return new ResponseEntity<>(checkAuthService.getResponse(), HttpStatus.OK);
    }

    @PostMapping("/login")
    private ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        return new ResponseEntity<>(loginService.getResponse(loginRequest), HttpStatus.OK);
    }
}