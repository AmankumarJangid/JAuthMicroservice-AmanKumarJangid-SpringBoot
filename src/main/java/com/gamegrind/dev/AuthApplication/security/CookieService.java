package com.gamegrind.dev.AuthApplication.security;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cookieSameSite;

    private final Logger logger = LoggerFactory.getLogger(CookieService.class);


    public CookieService(
            // "${security.jwt.refresh-token-coolie-name}"// same way
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-domain}" )String cookieDomain,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite
            ){
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }

    // create method to attack cookie to response
    public void attachRefreshCookie(HttpServletResponse response ,String value, long maxAge){
//        ResponseCookie.ResponseCookieBuilder cookieBuilder=
        logger.info("Attaching cookie with name : {} and value : {} ", refreshTokenCookieName, value);
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName , value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        if( cookieDomain!= null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();

        response.addHeader(HttpHeaders.SET_COOKIE , responseCookie.toString());

    }

    // clear refresh-cookie

    public void clearRefreshCookie(HttpServletResponse response){
        attachRefreshCookie(response , "" , 0);
    }

    // add not store headers
    public void addNoStoreHeaders(HttpServletResponse response){
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
