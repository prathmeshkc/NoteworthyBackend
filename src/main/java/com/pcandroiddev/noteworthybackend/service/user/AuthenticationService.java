package com.pcandroiddev.noteworthybackend.service.user;

import com.pcandroiddev.noteworthybackend.dao.user.UserDao;
import com.pcandroiddev.noteworthybackend.model.response.AuthenticationResponse;
import com.pcandroiddev.noteworthybackend.model.request.LoginRequest;
import com.pcandroiddev.noteworthybackend.model.request.RegisterRequest;
import com.pcandroiddev.noteworthybackend.model.exception.MessageBody;
import com.pcandroiddev.noteworthybackend.model.user.Role;
import com.pcandroiddev.noteworthybackend.model.user.User;
import com.pcandroiddev.noteworthybackend.service.jwt.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        if (savedUser instanceof MessageBody) {
            return ResponseEntity.badRequest().body(savedUser);
        }

        if (savedUser == null) {
            return ResponseEntity.internalServerError().body(new MessageBody("Something Went Wrong!"));
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
            return ResponseEntity.badRequest().body(new MessageBody("User not found!"));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        }catch (BadCredentialsException badCredentialsException){
            return ResponseEntity.badRequest().body(new MessageBody("Incorrect Username or Password!"));
        }catch (LockedException lockedException){
            return ResponseEntity.badRequest().body(new MessageBody("Account is Locked!"));
        }catch (DisabledException disabledException){
            return ResponseEntity.badRequest().body(new MessageBody("Account is Disabled!"));
        }


        var jwtToken = jwtService.generateTokenFromUserDetails(user);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .user(user)
                .token(jwtToken)
                .build());


    }
}
