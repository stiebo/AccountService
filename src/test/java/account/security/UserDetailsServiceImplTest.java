package account.security;

import account.domain.entities.User;
import account.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void loadUserByUsername_found_returnsUser() {
        User user = new User();
        user.setUsername("user@acme.com");
        when(repository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(user));

        var result = service.loadUserByUsername("user@acme.com");

        assertEquals(user, result);
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(repository.findByUsernameIgnoreCase("nobody@acme.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nobody@acme.com"));
    }
}
