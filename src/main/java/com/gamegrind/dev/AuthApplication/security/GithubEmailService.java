package com.gamegrind.dev.AuthApplication.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gamegrind.dev.AuthApplication.dtos.GithubEmailResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GithubEmailService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    private record AccessTokenResponse(
            @JsonProperty("access_token") String access_token,
            @JsonProperty("token_type") String token_type,
            @JsonProperty("scope") String scope
    ){}


    private record AccessTokenRequest(
            @JsonProperty("client_id") String clientId,
            @JsonProperty("client_secret") String clientSecret,
            @JsonProperty("code") String code
        ){}

    private final Logger logger = LoggerFactory.getLogger(GithubEmailService.class);


    private final String clientId;
    private final String clientSecret;
    private final RestClient authClient;
    private final RestClient apiClient;


    GithubEmailService(
            OAuth2AuthorizedClientService authorizedClientService, @Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.github.client-secret}") String clientSecret
    ){
        this.authorizedClientService = authorizedClientService;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authClient = RestClient.builder().baseUrl("https://github.com").build();
        this.apiClient = RestClient.builder().baseUrl("https://api.github.com").build();

    }

    @Deprecated
    /*
    This is not used as the requset made now needs the Authentication.class
    As the code is intercepted inbetween by the OAuth2AuthorizedClientSerice and Stores the code and uses it fetch the accessToken
    We doesn't need to fetch the access token manually
     */
    public String fetchPrimaryEmail(HttpServletRequest request){
        AccessTokenResponse accessTokenResponse = getAccessToken(request);

        logger.info("Captured Token Value: {}", accessTokenResponse.access_token());

        return getPrimaryEmail(accessTokenResponse.access_token());
    }

    public String fetchPrimaryEmail(Authentication authentication){
        if( !(authentication instanceof OAuth2AuthenticationToken oAuth2Token)){
            logger.warn("Invalid authentication type for direct GitHub API token lookup.");
            return "";
        }

        var authorizedClient = authorizedClientService.loadAuthorizedClient(
                oAuth2Token.getAuthorizedClientRegistrationId(),
                oAuth2Token.getName()
        );

        if( authorizedClient == null || authorizedClient.getAccessToken() == null){
            logger.warn("Could not find an active OAuth2 access token in the security context.");
            return "";
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        logger.info("[Inside FETCH_PRIMARY_EMAIL METHOD]Successfully recovered Spring-managed OAuth token. Proceeding to fetch emails...");

        return getPrimaryEmail(accessToken);
    }


    @Deprecated
    private AccessTokenResponse getAccessToken(HttpServletRequest request) {
        String code = request.getParameter("code");

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("OAuth2 authorization code is missing from the request");
        }

        logger.info("[Inside GetAccessToken METHOD] : Captured code value: {}\n" +
                "client_id : {} and clientSecret : {} ", code, clientId, clientSecret);

        MultiValueMap<String, String> tokenFormPayload = new LinkedMultiValueMap<>();
        tokenFormPayload.add("client_id", clientId);
        tokenFormPayload.add("client_secret", clientSecret);
        tokenFormPayload.add("code", code);

        AccessTokenResponse response = null;

        String rawResponse= authClient.post()
                .uri("/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Accept", "application/json")
                .header("User-Agent","JAuthMicroservice-AmanKumarJangid")
                .body(tokenFormPayload)
                .retrieve()
                .body(String.class  );


        if (rawResponse == null || rawResponse.isBlank()) {
            logger.info("[Inside GetAccessToken METHOD] : Raw response string is null/empty");
            return new AccessTokenResponse(null, null, null);
        }

        logger.info("[Inside GetAccessToken METHOD] : Raw response string: {}", rawResponse);

//        if( response.access_token() == null) logger.info("[Inside GetAccessToken METHOD] : AccessTokenResponse is null");
//        else logger.info("AccessTokenResponse: {}", response.access_token());

        return new AccessTokenResponse(null, null, null);
    }

    private String getPrimaryEmail(String accessToken) {
        try {
            List<GithubEmailResponse> emails = apiClient.get()
                    .uri("/user/emails")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2026-03-10")
                    .header("User-Agent","JAuthMicroservice-AmanKumarJangid")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GithubEmailResponse>>() {
                    });

            if (emails != null) {
                return emails.stream()
                        .filter(e -> e.primary() && e.verified())
                        .map(GithubEmailResponse::email)
                        .findFirst()
                        .orElse("");
            }
        }
        catch(Exception e){
            logger.error("Error executing background third-party call to GitHub emails endpoint: {}", e.getMessage());
        }

        return "";
    }


}
