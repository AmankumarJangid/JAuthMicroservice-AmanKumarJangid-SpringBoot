package com.gamegrind.dev.AuthApplication.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

// as Refresh Token will be frequently used so we will use Index
//
@Entity
@Table(name = "refresh_token",indexes = { // jti and user_id should be unique as we will use them to find the refresh token
    @Index(name = "refresh_token_jti_idx", columnList = "jti" , unique = true),
    @Index(name = "refresh_token_user_id_idx", columnList = "user_id"), // user_id need not be unique as user_id can have multiple refresh_token

})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "jti", unique = true,nullable = false, updatable = false)
    private String jti;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked;

    private String replacedByToken;
}
