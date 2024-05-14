package account.security;

import account.business.EventName;
import account.business.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityService logger;

    @Autowired
    public CustomAccessDeniedHandler (SecurityService securityService) {
        this.logger = securityService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        logger.logEvent(EventName.ACCESS_DENIED,
                SecurityContextHolder.getContext().getAuthentication().getName(),
                request.getRequestURI(),
                request.getRequestURI());
        response.sendError(HttpServletResponse.SC_FORBIDDEN,"Access Denied!");
    }
}