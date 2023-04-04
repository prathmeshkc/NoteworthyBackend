package com.pcandroiddev.noteworthybackend.service.user;

import com.pcandroiddev.noteworthybackend.dao.UserDao;
import com.pcandroiddev.noteworthybackend.model.auth.AuthenticationResponse;
import com.pcandroiddev.noteworthybackend.model.auth.LoginRequest;
import com.pcandroiddev.noteworthybackend.model.auth.RegisterRequest;
import com.pcandroiddev.noteworthybackend.model.exception.ExceptionBody;
import com.pcandroiddev.noteworthybackend.model.user.Role;
import com.pcandroiddev.noteworthybackend.model.user.User;
import com.pcandroiddev.noteworthybackend.service.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;


    public ResponseEntity<?> register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        Object savedUser = userDao.saveUser(user);

        if (savedUser instanceof ExceptionBody) {
            return ResponseEntity.badRequest().body(savedUser);
        }

        if (savedUser == null) {
            return ResponseEntity.internalServerError().body(new ExceptionBody("Something Went Wrong!"));
        }

        var jwtToken = jwtService.generateTokenFromUserDetails((User) savedUser);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .user((User) savedUser)
                .token(jwtToken)
                .build());
    }

    public ResponseEntity<?> login(LoginRequest request) {

        var user = userDao.findByEmail(request.getEmail());

        if (user == null) {
            return ResponseEntity.badRequest().body(new ExceptionBody("User not found!"));
        }


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var jwtToken = jwtService.generateTokenFromUserDetails(user);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .user(user)
                .token(jwtToken)
                .build());


    }
}
