package com.pcandroiddev.noteworthybackend.controller;

import com.pcandroiddev.noteworthybackend.dao.user.UserDao;
import com.pcandroiddev.noteworthybackend.model.request.LoginRequest;
import com.pcandroiddev.noteworthybackend.model.request.RegisterRequest;
import com.pcandroiddev.noteworthybackend.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
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
    private final AuthenticationService authenticationService;


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
}
