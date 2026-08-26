package com.gamegrind.dev.AuthApplication.security;

import com.gamegrind.dev.AuthApplication.entities.Role;
import com.gamegrind.dev.AuthApplication.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
public class JwtService {

    private final SecretKey key;
    private final long accessTtlseconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    private JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlseconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds ,
            @Value("${security.jwt.issuer}") String issuer){

        if( secret==null || secret.length() < 64  ){
            throw new IllegalArgumentException("Invalid Secret : JWT secret must be at least 64 characters long");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        this.accessTtlseconds = accessTtlseconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    // generate access token
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // setId is depreceted instead only id and subject , issuer and issuedAt , expiration and claims are supported
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlseconds)))
                .claims(Map.of(
                        "email" , user.getEmail(),
                        "roles", roles,
                        "typ", "access"
                ))
                .signWith(key)
                .compact();
    }

    // generate refresh token
    public String generateRefreshToken(User user , String jti) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        // jti is actual id for the refreshToken being stored in the database
        return Jwts.builder()
                .id(jti) // setId is depreceted instead only id and subject , issuer and issuedAt , expiration and claims are supported
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ", "refresh")
                .signWith(key)
                .compact();
    }

    // parse the token
    public Jws<Claims> parseToken(String token) {
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }
        catch (JwtException error){
            throw error;
        }
    }

    public boolean isAccessToken(String token){
        Claims c = parseToken(token).getPayload(); // getPayload() instead of getBody() because we are using parseSignedClaims() instead of parseClaimsJws()
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims c = parseToken(token).getPayload(); // getPayload() instead of getBody() because we are using parseSignedClaims() instead of parseClaimsJws()
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token){
        Claims c = parseToken(token).getPayload(); // getPayload() instead of getBody() because we are using parseSignedClaims() instead of parseClaimsJws()
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token){
        Claims c = parseToken(token).getPayload(); // getPayload() instead of getBody() because we are using parseSignedClaims() instead of parseClaimsJws()
        return c.getId();
    }

    public List<String> getRoles(String token){
        Claims c = parseToken(token).getPayload(); // getPayload() instead of getBody() because we are using parseSignedClaims() instead of parseClaimsJws()
        return (List<String>) c.get("roles", List.class);

    }

    public String getEmail(String token){
        Claims c = parseToken(token).getPayload(); // getPayload() instead of getBody() because we are using parseSignedClaims() instead of parseClaimsJws()
        return c.get("email", String.class);

    }


    // verify token

    // get claims from token
}
