package com.pcandroiddev.noteworthybackend.controller;

import com.pcandroiddev.noteworthybackend.model.request.LoginRequest;
import com.pcandroiddev.noteworthybackend.model.request.RefreshTokenRequest;
import com.pcandroiddev.noteworthybackend.model.request.RegisterRequest;
import com.pcandroiddev.noteworthybackend.service.jwt.RefreshTokenService;
import com.pcandroiddev.noteworthybackend.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request
    ) {
        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {
        return authenticationService.login(request);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenRequest refreshTokenRequest
    ) {
        return authenticationService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(
            @RequestBody RefreshTokenRequest refreshTokenRequest
    ) {
        authenticationService.logout(refreshTokenRequest);
        return ResponseEntity.ok().build();
    }
}
