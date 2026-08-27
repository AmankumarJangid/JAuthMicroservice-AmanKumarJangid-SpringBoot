package com.gamegrind.dev.AuthApplication.security;

import com.gamegrind.dev.AuthApplication.entities.Provider;
import com.gamegrind.dev.AuthApplication.entities.RefreshToken;
import com.gamegrind.dev.AuthApplication.entities.User;
import com.gamegrind.dev.AuthApplication.repositories.RefreshTokenRepository;
import com.gamegrind.dev.AuthApplication.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class Outh2SuccessHandler implements AuthenticationSuccessHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GithubEmailService githubEmailService;

    @Value("${app.auth.frontend.success-redirect}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("Successfully authenticated user: " + authentication.getName());
        logger.info("Http request : \n {}" , Arrays.toString(request.getParameterValues("code")));
        // You can add additional logic here, such as redirecting the user to a specific page
        var oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = "unknown";

        if( authentication instanceof OAuth2AuthenticationToken token) {
            registrationId = token.getAuthorizedClientRegistrationId();
        }

        logger.info("registrationid : " + registrationId);
        assert oAuth2User != null;
        logger.info("user: "  + oAuth2User.getAttributes().toString());

        User user = null;

        switch( registrationId ){
            case "google" -> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                user = userRepository.findByEmail(email).map(existingUser ->{
                    logger.info("User Already Exists with email on google : {} " , existingUser.getEmail());
                    return existingUser;
                }).orElseGet(()-> {
                            User newUser = User.builder()
                                    .email(email)
                                    .name(name)
                                    .image(picture)
                                    .provider(Provider.GOOGLE)
                                    .providerId(googleId)
                                    .enable(true)
                                    .build();

                            userRepository.save(newUser);
                            return newUser;
                        }
                );
            }
            case "github" ->{
                String githubId = oAuth2User.getAttributes().getOrDefault("login", "").toString();
                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

                if(email.isBlank()){
                    email = githubEmailService.fetchPrimaryEmail(authentication);
                }

                String finalEmail = email;

                user = userRepository.findByEmail(email).map(existingUser-> {
                    logger.info("User Already Exists with email on github : {} " , existingUser.getEmail());
                    return existingUser;
                }).orElseGet(()-> {
                            User newUser = User.builder()
                                    .email(finalEmail)
                                    .name(name)
                                    .image(picture)
                                    .provider(Provider.GITHUB)
                                    .providerId(githubId)
                                    .enable(true)
                                    .build();

                            userRepository.save(newUser);
                            return newUser;
                        }
                );



            }
            default -> {
//                logger.warn("Unsupported registrationId: " + registrationId);
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported registrationId: " + registrationId);
//                return;
                throw new RuntimeException("Invalid registration id");
            }
        }

        // we got username, email ,
        // we just need to create a new user from it

        // and send back the jwt access token and refreshToken
        // we only give the refresh token

        String jti = UUID.randomUUID().toString();
        var refreshTokenObject = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();

        refreshTokenRepository.save(refreshTokenObject);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, jti);

        cookieService.attachRefreshCookie(response, refreshToken, jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);


        response.sendRedirect(frontendSuccessUrl);
    }



    private record GithubEmailResponse(String email, boolean primary, boolean verified, String visibility) {}

    private String getVerifiedEmailFromGithub(Authentication authentication, OAuth2User oAuth2User) {

        AtomicReference<String> emailRef = new AtomicReference<>("");
        try {
            var client = authorizedClientService.loadAuthorizedClient(
                    ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            if (client != null) {
                var accessToken = client.getAccessToken().getTokenValue();
                var url = "https://api.github.com/user/emails";
                var request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/vnd.github.v3+json")
                        .GET()
                        .build();

                var httpClient = HttpClient.newHttpClient();
                var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    var objectMapper = new ObjectMapper();
                    GithubEmailResponse[] emails = objectMapper.readValue(response.body(), GithubEmailResponse[].class);

                    for (GithubEmailResponse emailResponse : emails) {
                        if (emailResponse.verified() && emailResponse.primary()) {
                            emailRef.set(emailResponse.email());
                            break;
                        }
                    }
                } else {
                    logger.error("Failed to fetch emails from GitHub: HTTP " + response.statusCode());
                }
            } else {
                logger.error("Authorized client is null for user: " + authentication.getName());
            }
        } catch (Exception e) {
            logger.error("Error fetching verified email from GitHub", e);
        }

        return emailRef.get();
    }

}
