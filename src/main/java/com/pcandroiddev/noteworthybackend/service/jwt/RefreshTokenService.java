package com.pcandroiddev.noteworthybackend.service.jwt;

import com.pcandroiddev.noteworthybackend.model.jwt.RefreshToken;
import com.pcandroiddev.noteworthybackend.repository.RefreshTokenRepository;
import com.pcandroiddev.noteworthybackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    @Autowired
    private UserRepository userRepository;

    public RefreshToken createRefreshToken(String userEmail) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findByEmail(userEmail).get())
                .token(UUID.randomUUID().toString())
                .expiryInstance(Instant.now().plusMillis(1000 * 60 * 5))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    //Change the return type to Object and return a ResponseEntity with appropriate status
    //code and message: "Refresh token has expired. Make a new login request!"
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryInstance().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + "Refresh token has expired. Make a new login request!");
        }
        return token;
    }

    public void deleteRefreshToken(String token) {
        Optional<RefreshToken> tokenOptional = findByToken(token);
        tokenOptional.ifPresent(refreshToken -> refreshTokenRepository.delete(refreshToken));
    }


}
