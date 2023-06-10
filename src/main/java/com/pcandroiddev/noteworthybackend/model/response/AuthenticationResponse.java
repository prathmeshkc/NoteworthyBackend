package com.pcandroiddev.noteworthybackend.model.response;

import com.pcandroiddev.noteworthybackend.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private User user;
    private String token;
    private String refreshToken;
}
