package ru.xgodness.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import ru.xgodness.security.filter.FilterChainExceptionHandler;
import ru.xgodness.security.filter.JwtFilter;
import ru.xgodness.security.filter.URILengthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Getter
    private static final String[] noAuthorizationPaths = new String[]{
            "/auth/register",
            "/auth/login",
            "/auth/token/access",
            "/health"
    };

    private final URILengthFilter uriLengthFilter;
    private final FilterChainExceptionHandler filterChainExceptionHandler;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionMgmt -> sessionMgmt.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(noAuthorizationPaths).permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterAfter(uriLengthFilter, X509AuthenticationFilter.class)
                .addFilterAfter(filterChainExceptionHandler, URILengthFilter.class)
                .addFilterAfter(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
