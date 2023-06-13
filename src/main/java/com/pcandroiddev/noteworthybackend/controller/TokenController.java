package com.pcandroiddev.noteworthybackend.controller;

import com.pcandroiddev.noteworthybackend.model.jwt.RefreshTokenRequest;
import com.pcandroiddev.noteworthybackend.service.user.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class TokenController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/refreshJWT")
    public ResponseEntity<?> refreshJWT(@RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshJWT(request);
    }
}
