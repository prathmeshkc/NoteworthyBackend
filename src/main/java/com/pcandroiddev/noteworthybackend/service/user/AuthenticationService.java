package com.pcandroiddev.noteworthybackend.service.user;

import com.pcandroiddev.noteworthybackend.model.exception.MessageBody;
import com.pcandroiddev.noteworthybackend.model.jwt.RefreshToken;
import com.pcandroiddev.noteworthybackend.model.request.LoginRequest;
import com.pcandroiddev.noteworthybackend.model.request.RefreshTokenRequest;
import com.pcandroiddev.noteworthybackend.model.request.RegisterRequest;
import com.pcandroiddev.noteworthybackend.model.response.AuthenticationResponse;
import com.pcandroiddev.noteworthybackend.model.user.Role;
import com.pcandroiddev.noteworthybackend.model.user.User;
import com.pcandroiddev.noteworthybackend.repository.UserRepository;
import com.pcandroiddev.noteworthybackend.service.jwt.JWTService;
import com.pcandroiddev.noteworthybackend.service.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;


    public ResponseEntity<?> register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        Optional<User> savedUser = userRepository.findByEmail(request.getEmail());
        if (savedUser.isPresent()) {
            return ResponseEntity.badRequest().body(new MessageBody("User already exists!"));
        }

        try {
            User newUser = userRepository.save(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getEmail());
            var jwtToken = jwtService.generateTokenFromUserDetails(newUser);
            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .user(newUser)
                    .token(jwtToken)
                    .refreshToken(refreshToken.getToken())
                    .build());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
        }
    }

    public ResponseEntity<?> login(LoginRequest request) {


        var user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageBody("User not found!"));
        }


        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException badCredentialsException) {
            return ResponseEntity.badRequest().body(new MessageBody("Incorrect Username or Password!"));
        } catch (LockedException lockedException) {
            return ResponseEntity.badRequest().body(new MessageBody("Account is Locked!"));
        } catch (DisabledException disabledException) {
            return ResponseEntity.badRequest().body(new MessageBody("Account is Disabled!"));
        }

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getEmail());
        var jwtToken = jwtService.generateTokenFromUserDetails(user.get());
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .user(user.get())
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build());
    }

    public ResponseEntity<?> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return refreshTokenService.findByToken(refreshTokenRequest.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String jwtToken = jwtService.generateTokenFromUserDetails(user);
                    return ResponseEntity.ok(AuthenticationResponse.builder()
                            .user(user)
                            .token(jwtToken)
                            .refreshToken(refreshTokenRequest.getToken())
                            .build());
                }).orElseThrow(() -> new RuntimeException("Refresh token not found in database!"));
    }

    public void logout(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getToken());
    }

}
