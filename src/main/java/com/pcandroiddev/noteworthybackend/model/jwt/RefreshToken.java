package com.pcandroiddev.noteworthybackend.model.jwt;

import com.pcandroiddev.noteworthybackend.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Remember, refresh token is not the actual JWT. It is just a random UUID string associated
 * with a particular user for that time period and can be used to generate a new JWT.
 */

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String token;

    private Instant expiryInstance;

    @OneToOne
    @JoinColumn(name = "user_Id", referencedColumnName = "id", nullable = false)
    private User user;
}
