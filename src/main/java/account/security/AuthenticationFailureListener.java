package account.security;

import account.business.AdminService;
import account.business.EventName;
import account.business.SecurityService;
import account.domain.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationFailureListener implements
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final AdminService service;
    private final HttpServletRequest request;
    private final SecurityService logger;

    @Autowired
    public AuthenticationFailureListener (AdminService adminService, HttpServletRequest request,
                                          SecurityService securityService) {
        this.service = adminService;
        this.request = request;
        this.logger = securityService;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getPrincipal().toString();
        logger.logEvent(EventName.LOGIN_FAILED,
                username,
                request.getRequestURI(),
                request.getRequestURI());
        Optional<User> optionalUser = service.getUserByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!service.hasAdminRole(user) && user.isEnabled() && user.isAccountNonLocked()) {
                if (user.getFailedAttempts() < service.MAX_FAILED_ATTEMPTS - 1) {
                    service.increaseFailedAttempts(user);
                } else {
                    service.bruteForceLockUser(user);
                    logger.logEvent(EventName.BRUTE_FORCE,
                            user.getUsername(),
                            request.getRequestURI(),
                            request.getRequestURI());
                    logger.logEvent(EventName.LOCK_USER,
                            user.getUsername(),
                            "Lock user " + user.getUsername(),
                            request.getRequestURI());
                }
            }
        }
    }
}