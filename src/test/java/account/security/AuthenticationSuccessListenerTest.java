package account.security;

import account.business.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationSuccessListenerTest {

    @Mock
    private AdminService service;

    @InjectMocks
    private AuthenticationSuccessListener listener;

    @Test
    void onApplicationEvent_resetsFailedAttempts() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@acme.com");
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);

        listener.onApplicationEvent(event);

        verify(service).resetFailedAttempts("user@acme.com");
    }
}
