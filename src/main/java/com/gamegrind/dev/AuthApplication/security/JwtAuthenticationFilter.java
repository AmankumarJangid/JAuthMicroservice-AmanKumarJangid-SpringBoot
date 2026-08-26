package com.gamegrind.dev.AuthApplication.security;

import com.gamegrind.dev.AuthApplication.helpers.UserHelper;
import com.gamegrind.dev.AuthApplication.repositories.UserRepository;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtServcie;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String  header = request.getHeader("Authorization");

        logger.info("Request URL : {} , Authorization Header : {}", request.getRequestURL(), header);

//        if( request.getServletPath().startsWith("/api/v1/auth")){
//            filterChain.doFilter(request, response);
//            return ;
//        }
        if ( header != null && header.startsWith("Bearer ")){

            // token extract and validate then authenticate the user and set the authentication in the security context
            String token = header.substring(7); // actual token without "Bearer "
            logger.info("Autherization header found : {}", header);


            try{

                if( !jwtServcie.isAccessToken(token)){
                    // message pass karna ho to
                    logger.info("Given key is not access token");
                    filterChain.doFilter(request, response);
                    return ;
                }
                Jws<Claims> parse =  jwtServcie.parseToken(token);
                Claims payload = parse.getPayload();

                String userId = payload.getSubject();
                UUID userUuid = UserHelper.parseUserId(userId); // can also use UUID.parseString(userId) but this is more robust and provides better error handling

                userRepository.findById(userUuid)
                        .ifPresent(user -> {



                            if( user.isEnable()){
                                List<GrantedAuthority> authorities= user.getRoles() == null ? List.of() :
                                        user.getRoles().stream()
                                                .map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
                                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        authorities
                                );

                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                // final line to set the authentication in the security context\
                                if( SecurityContextHolder.getContext().getAuthentication() == null){
                                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                                }
                            }


                        });

            }catch(ExpiredJwtException e){
                e.printStackTrace();

            }catch(MalformedJwtException e){
                e.printStackTrace();
            }
            catch(JwtException e){
                e.printStackTrace();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

            filterChain.doFilter(request, response);
    }
}
