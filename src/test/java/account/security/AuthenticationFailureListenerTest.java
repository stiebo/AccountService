package account.security;

import account.business.AdminService;
import account.business.EventName;
import org.springframework.security.authentication.BadCredentialsException;
import account.business.SecurityService;
import account.domain.entities.Group;
import account.domain.entities.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationFailureListenerTest {

    @Mock
    private AdminService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityService logger;

    @InjectMocks
    private AuthenticationFailureListener listener;

    private User regularUser;

    @BeforeEach
    void setUp() {
        Group userGroup = new Group("ROLE_USER", "business");
        regularUser = new User();
        regularUser.setUsername("user@acme.com");
        regularUser.setGroups(new ArrayList<>(List.of(userGroup)));
        regularUser.setAccountNonLocked(true);
        regularUser.setFailedAttempts(0);

        when(request.getRequestURI()).thenReturn("/api/empl/payment");
    }

    private AuthenticationFailureBadCredentialsEvent createEvent(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(username);
        return new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));
    }

    @Test
    void onApplicationEvent_userNotInDb_logsLoginFailed() {
        when(service.getUserByUsername("unknown@acme.com")).thenReturn(Optional.empty());
        AuthenticationFailureBadCredentialsEvent event = createEvent("unknown@acme.com");

        listener.onApplicationEvent(event);

        verify(logger).logEvent(eq(EventName.LOGIN_FAILED), eq("unknown@acme.com"),
                anyString(), anyString());
        verify(service, never()).increaseFailedAttempts(any());
        verify(service, never()).bruteForceLockUser(any());
    }

    @Test
    void onApplicationEvent_adminUser_logsButDoesNotLock() {
        Group adminGroup = new Group("ROLE_ADMINISTRATOR", "admin");
        User admin = new User();
        admin.setUsername("admin@acme.com");
        admin.setGroups(new ArrayList<>(List.of(adminGroup)));
        admin.setAccountNonLocked(true);
        admin.setFailedAttempts(0);

        when(service.getUserByUsername("admin@acme.com")).thenReturn(Optional.of(admin));
        when(service.hasAdminRole(admin)).thenReturn(true);
        AuthenticationFailureBadCredentialsEvent event = createEvent("admin@acme.com");

        listener.onApplicationEvent(event);

        verify(service, never()).increaseFailedAttempts(any());
        verify(service, never()).bruteForceLockUser(any());
    }

    @Test
    void onApplicationEvent_regularUser_belowMaxAttempts_incrementsAttempts() {
        regularUser.setFailedAttempts(2);
        when(service.getUserByUsername("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(service.hasAdminRole(regularUser)).thenReturn(false);
        AuthenticationFailureBadCredentialsEvent event = createEvent("user@acme.com");

        listener.onApplicationEvent(event);

        verify(service).increaseFailedAttempts(regularUser);
        verify(service, never()).bruteForceLockUser(any());
    }

    @Test
    void onApplicationEvent_regularUser_atMaxAttempts_bruteForceLocks() {
        regularUser.setFailedAttempts(AdminService.MAX_FAILED_ATTEMPTS - 1);
        when(service.getUserByUsername("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(service.hasAdminRole(regularUser)).thenReturn(false);
        AuthenticationFailureBadCredentialsEvent event = createEvent("user@acme.com");

        listener.onApplicationEvent(event);

        verify(service).bruteForceLockUser(regularUser);
        verify(logger).logEvent(eq(EventName.BRUTE_FORCE), anyString(), anyString(), anyString());
        verify(logger).logEvent(eq(EventName.LOCK_USER), anyString(), anyString(), anyString());
    }

    @Test
    void onApplicationEvent_userAlreadyLocked_doesNotIncrementOrLock() {
        regularUser.setAccountNonLocked(false);
        when(service.getUserByUsername("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(service.hasAdminRole(regularUser)).thenReturn(false);
        AuthenticationFailureBadCredentialsEvent event = createEvent("user@acme.com");

        listener.onApplicationEvent(event);

        verify(service, never()).increaseFailedAttempts(any());
        verify(service, never()).bruteForceLockUser(any());
    }
}
