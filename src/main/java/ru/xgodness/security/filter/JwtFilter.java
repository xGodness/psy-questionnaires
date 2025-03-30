package ru.xgodness.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import ru.xgodness.security.JwtAuthentication;
import ru.xgodness.security.util.JwtProvider;
import ru.xgodness.security.util.JwtUtils;

import java.io.IOException;
import java.util.Set;

@Log
@Component
public class JwtFilter extends GenericFilterBean {

    private final static String AUTHORIZATION = "Authorization";

    private final JwtProvider jwtProvider;
    private final Set<String> noAuthorizationPaths;


    public JwtFilter(JwtProvider jwtProvider,
                     @Value("${server.servlet.context-path}") String contextPath) {
        this.jwtProvider = jwtProvider;

        noAuthorizationPaths = Set.of(
                contextPath + "/auth/register",
                contextPath + "/auth/login",
                contextPath + "/auth/token/access"
        );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        if (!noAuthorizationPaths.contains(((HttpServletRequest) request).getRequestURI())) {
            String token = getTokenFromRequest((HttpServletRequest) request);
            if (token != null)
                jwtProvider.validateAccessToken(token);

            Claims claims = jwtProvider.getAccessClaims(token);
            JwtAuthentication authentication = JwtUtils.generateAuthentication(claims);
            authentication.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION);
        return (StringUtils.hasText(bearer) && bearer.startsWith("Bearer "))
                ? bearer.substring(7)
                : null;
    }

}
