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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class ApiAuthController {

    private final CaptchaService captchaService;
    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity<ResultResponse> check(Principal principal) {
        return new ResponseEntity<>(userService.checkAuth(principal), HttpStatus.OK);
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> captcha(){
        return new ResponseEntity<>(captchaService.generateCaptcha(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ResultResponse> register(@RequestBody AddUserRequest addUserRequest) {
        return new ResponseEntity<>(userService.registration(addUserRequest), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ResultResponse> login(@RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(userService.login(loginRequest), HttpStatus.OK);
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> logout(){
        return new ResponseEntity<>(userService.logout(), HttpStatus.OK);
    }
}