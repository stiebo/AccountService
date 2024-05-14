package account.security;

import account.business.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements
        ApplicationListener<AuthenticationSuccessEvent> {

    private final AdminService service;

    @Autowired
    public AuthenticationSuccessListener (AdminService adminService) {
        this.service = adminService;
    }

    @Override
    public void onApplicationEvent (AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        service.resetFailedAttempts(username);
    }
}