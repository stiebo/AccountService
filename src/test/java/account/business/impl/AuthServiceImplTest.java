package account.business.impl;

import account.business.exception.PasswordException;
import account.business.exception.UserExistsException;
import account.domain.entities.Group;
import account.domain.entities.User;
import account.repository.GroupRepository;
import account.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private AuthServiceImpl service;

    private User createUser(String password) {
        return new User()
                .setName("John")
                .setLastname("Doe")
                .setEmail("john@acme.com")
                .setUsername("john@acme.com")
                .setPassword(password);
    }

    @Test
    void registerUser_firstUser_getsAdminRole() {
        User user = createUser("validpassword12");
        Group adminGroup = new Group("ROLE_ADMINISTRATOR", "admin");
        when(userRepository.existsByUsernameIgnoreCase("john@acme.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(0L);
        when(groupRepository.findByName("ROLE_ADMINISTRATOR")).thenReturn(adminGroup);
        when(encoder.encode("validpassword12")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerUser(user);

        assertTrue(result.getGroups().contains(adminGroup));
        assertTrue(result.isAccountNonLocked());
        assertEquals(0, result.getFailedAttempts());
    }

    @Test
    void registerUser_notFirstUser_getsUserRole() {
        User user = createUser("validpassword12");
        Group userGroup = new Group("ROLE_USER", "business");
        when(userRepository.existsByUsernameIgnoreCase("john@acme.com")).thenReturn(false);
        when(userRepository.count()).thenReturn(1L);
        when(groupRepository.findByName("ROLE_USER")).thenReturn(userGroup);
        when(encoder.encode("validpassword12")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = service.registerUser(user);

        assertTrue(result.getGroups().contains(userGroup));
    }

    @Test
    void registerUser_userExists_throwsUserExistsException() {
        User user = createUser("validpassword12");
        when(userRepository.existsByUsernameIgnoreCase("john@acme.com")).thenReturn(true);

        assertThrows(UserExistsException.class, () -> service.registerUser(user));
    }

    @Test
    void registerUser_passwordTooShort_throwsPasswordException() {
        User user = createUser("short");
        when(userRepository.existsByUsernameIgnoreCase("john@acme.com")).thenReturn(false);

        PasswordException ex = assertThrows(PasswordException.class, () -> service.registerUser(user));
        assertEquals("Password length must be 12 chars minimum!", ex.getMessage());
    }

    @Test
    void registerUser_passwordInBreaches_throwsPasswordException() {
        User user = createUser("PasswordForJanuary");
        when(userRepository.existsByUsernameIgnoreCase("john@acme.com")).thenReturn(false);

        PasswordException ex = assertThrows(PasswordException.class, () -> service.registerUser(user));
        assertEquals("The password is in the hacker's database!", ex.getMessage());
    }

    @Test
    void changePassword_success_encodesAndSaves() {
        User user = createUser("encodedOldPassword");
        when(encoder.matches("newValidPassword12", "encodedOldPassword")).thenReturn(false);
        when(encoder.encode("newValidPassword12")).thenReturn("encodedNew");

        service.changePassword(user, "newValidPassword12");

        verify(userRepository).save(user);
        assertEquals("encodedNew", user.getPassword());
        assertEquals(0, user.getFailedAttempts());
    }

    @Test
    void changePassword_samePassword_throwsPasswordException() {
        User user = createUser("encodedOldPassword");
        when(encoder.matches("newValidPassword12", "encodedOldPassword")).thenReturn(true);

        PasswordException ex = assertThrows(PasswordException.class,
                () -> service.changePassword(user, "newValidPassword12"));
        assertEquals("The passwords must be different!", ex.getMessage());
    }

    @Test
    void changePassword_tooShort_throwsPasswordException() {
        User user = createUser("encodedOldPassword");

        PasswordException ex = assertThrows(PasswordException.class,
                () -> service.changePassword(user, "short"));
        assertEquals("Password length must be 12 chars minimum!", ex.getMessage());
    }

    @Test
    void changePassword_passwordInBreaches_throwsPasswordException() {
        User user = createUser("encodedOldPassword");

        PasswordException ex = assertThrows(PasswordException.class,
                () -> service.changePassword(user, "PasswordForFebruary"));
        assertEquals("The password is in the hacker's database!", ex.getMessage());
    }
}
