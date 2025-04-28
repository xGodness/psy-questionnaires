package ru.xgodness.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import ru.xgodness.security.exception.URITooLongException;

import java.io.IOException;

@Log
@Component
public class URILengthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest httpServletRequest) {
            String uriWithQuery = "%s?%s".formatted(httpServletRequest.getRequestURI(), httpServletRequest.getQueryString());

            if (uriWithQuery.length() > 1024) {
                log.info("URILengthFilter: aborting request, URI too long");
                throw new URITooLongException();
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
