package account.business.impl;

import account.business.exception.AdminServiceException;
import account.business.exception.RoleNotFoundException;
import account.business.exception.UserNotFoundException;
import account.domain.dto.ChangeUserLockDto;
import account.domain.entities.Group;
import account.domain.entities.User;
import account.repository.GroupRepository;
import account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private AdminServiceImpl service;

    private User adminUser;
    private User regularUser;
    private Group adminGroup;
    private Group userGroup;
    private Group accountantGroup;

    @BeforeEach
    void setUp() {
        adminGroup = new Group("ROLE_ADMINISTRATOR", "admin");
        userGroup = new Group("ROLE_USER", "business");
        accountantGroup = new Group("ROLE_ACCOUNTANT", "business");

        adminUser = new User();
        adminUser.setUsername("admin@acme.com");
        adminUser.setGroups(new ArrayList<>(List.of(adminGroup)));
        adminUser.setAccountNonLocked(true);
        adminUser.setFailedAttempts(0);

        regularUser = new User();
        regularUser.setUsername("user@acme.com");
        regularUser.setGroups(new ArrayList<>(List.of(userGroup)));
        regularUser.setAccountNonLocked(true);
        regularUser.setFailedAttempts(0);
    }

    // --- getUserByUsername ---

    @Test
    void getUserByUsername_delegatesToRepository() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        Optional<User> result = service.getUserByUsername("user@acme.com");
        assertTrue(result.isPresent());
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(adminUser, regularUser));
        List<User> users = service.getAllUsers();
        assertEquals(2, users.size());
    }

    // --- deleteUser ---

    @Test
    void deleteUser_success_deletesUser() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        service.deleteUser("user@acme.com", adminUser);
        verify(userRepository).delete(regularUser);
    }

    @Test
    void deleteUser_notFound_throwsUserNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("nobody@acme.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.deleteUser("nobody@acme.com", adminUser));
    }

    @Test
    void deleteUser_selfAdmin_throwsAdminServiceException() {
        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.deleteUser("admin@acme.com", adminUser));
        assertEquals("Can't remove ADMINISTRATOR!", ex.getMessage());
    }

    // --- grantRole ---

    @Test
    void grantRole_success_grantsRole() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup, accountantGroup));
        when(groupRepository.findByName("ROLE_ACCOUNTANT")).thenReturn(accountantGroup);
        when(userRepository.save(regularUser)).thenReturn(regularUser);

        User result = service.grantRole("ROLE_ACCOUNTANT", "user@acme.com", adminUser);

        assertTrue(result.getGroups().contains(accountantGroup));
        verify(userRepository).save(regularUser);
    }

    @Test
    void grantRole_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("nobody@acme.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> service.grantRole("ROLE_ACCOUNTANT", "nobody@acme.com", adminUser));
    }

    @Test
    void grantRole_roleNotFound_throwsRoleNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup));

        assertThrows(RoleNotFoundException.class,
                () -> service.grantRole("ROLE_UNKNOWN", "user@acme.com", adminUser));
    }

    @Test
    void grantRole_roleCategoryViolation_throwsAdminServiceException() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup, accountantGroup));
        // regularUser has ROLE_USER (business), trying to grant ROLE_ADMINISTRATOR (admin)
        when(groupRepository.findByName("ROLE_ADMINISTRATOR")).thenReturn(adminGroup);

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.grantRole("ROLE_ADMINISTRATOR", "user@acme.com", adminUser));
        assertEquals("The user cannot combine administrative and business roles!", ex.getMessage());
    }

    @Test
    void grantRole_selfAdmin_throwsAdminServiceException() {
        // Admin user trying to change their own role; but first must pass category check
        // adminUser has ROLE_ADMINISTRATOR (admin); grant another admin role category
        Group anotherAdminRole = new Group("ROLE_ADMIN2", "admin");
        adminUser.setGroups(new ArrayList<>(List.of(adminGroup)));
        when(userRepository.findByUsernameIgnoreCase("admin@acme.com")).thenReturn(Optional.of(adminUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, anotherAdminRole));
        when(groupRepository.findByName("ROLE_ADMIN2")).thenReturn(anotherAdminRole);

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.grantRole("ROLE_ADMIN2", "admin@acme.com", adminUser));
        assertEquals("Can't change ADMINISTRATOR role!", ex.getMessage());
    }

    @Test
    void grantRole_alreadyHasRole_throwsAdminServiceException() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup, accountantGroup));
        when(groupRepository.findByName("ROLE_USER")).thenReturn(userGroup);

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.grantRole("ROLE_USER", "user@acme.com", adminUser));
        assertEquals("The user already has a role!", ex.getMessage());
    }

    // --- removeRole ---

    @Test
    void removeRole_success_removesRole() {
        regularUser.setGroups(new ArrayList<>(List.of(userGroup, accountantGroup)));
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup, accountantGroup));
        when(userRepository.save(regularUser)).thenReturn(regularUser);

        User result = service.removeRole("ROLE_ACCOUNTANT", "user@acme.com", adminUser);

        assertFalse(result.getGroups().contains(accountantGroup));
        verify(userRepository).save(regularUser);
    }

    @Test
    void removeRole_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("nobody@acme.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,
                () -> service.removeRole("ROLE_USER", "nobody@acme.com", adminUser));
    }

    @Test
    void removeRole_roleNotFound_throwsRoleNotFoundException() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup));

        assertThrows(RoleNotFoundException.class,
                () -> service.removeRole("ROLE_UNKNOWN", "user@acme.com", adminUser));
    }

    @Test
    void removeRole_userDoesNotHaveRole_throwsAdminServiceException() {
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup, accountantGroup));

        // regularUser only has ROLE_USER, not ROLE_ACCOUNTANT
        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.removeRole("ROLE_ACCOUNTANT", "user@acme.com", adminUser));
        assertEquals("The user does not have this role!", ex.getMessage());
    }

    @Test
    void removeRole_selfAdmin_throwsAdminServiceException() {
        // adminUser has ROLE_ADMINISTRATOR, admin group has name with ADMINISTRATOR
        // Need adminUser to have the role to remove, and two roles so last-role check passes
        Group anotherRole = new Group("ROLE_OTHER", "admin");
        adminUser.setGroups(new ArrayList<>(List.of(adminGroup, anotherRole)));
        when(userRepository.findByUsernameIgnoreCase("admin@acme.com")).thenReturn(Optional.of(adminUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, anotherRole));

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.removeRole("ROLE_ADMINISTRATOR", "admin@acme.com", adminUser));
        assertEquals("Can't remove ADMINISTRATOR role!", ex.getMessage());
    }

    @Test
    void removeRole_lastRole_throwsAdminServiceException() {
        // regularUser has only one role; remove it triggers last-role check
        // Need role to exist in repo and user to have the role
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));
        when(groupRepository.findAll()).thenReturn(List.of(adminGroup, userGroup));

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.removeRole("ROLE_USER", "user@acme.com", adminUser));
        assertEquals("The user must have at least one role!", ex.getMessage());
    }

    // --- lockUser ---

    @Test
    void lockUser_success_locksUser() {
        ChangeUserLockDto dto = new ChangeUserLockDto("user@acme.com", "LOCK");
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));

        service.lockUser(dto, adminUser);

        assertFalse(regularUser.isAccountNonLocked());
        verify(userRepository).save(regularUser);
    }

    @Test
    void lockUser_selfAdmin_throwsAdminServiceException() {
        ChangeUserLockDto dto = new ChangeUserLockDto("admin@acme.com", "LOCK");
        when(userRepository.findByUsernameIgnoreCase("admin@acme.com")).thenReturn(Optional.of(adminUser));

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.lockUser(dto, adminUser));
        assertEquals("Can't lock the ADMINISTRATOR!", ex.getMessage());
    }

    @Test
    void lockUser_alreadyLocked_throwsAdminServiceException() {
        regularUser.setAccountNonLocked(false);
        ChangeUserLockDto dto = new ChangeUserLockDto("user@acme.com", "LOCK");
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.lockUser(dto, adminUser));
        assertEquals("User already locked!", ex.getMessage());
    }

    // --- unlockUser ---

    @Test
    void unlockUser_success_unlocksUser() {
        regularUser.setAccountNonLocked(false);
        regularUser.setFailedAttempts(3);
        ChangeUserLockDto dto = new ChangeUserLockDto("user@acme.com", "UNLOCK");
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));

        service.unlockUser(dto, adminUser);

        assertTrue(regularUser.isAccountNonLocked());
        assertEquals(0, regularUser.getFailedAttempts());
        verify(userRepository).save(regularUser);
    }

    @Test
    void unlockUser_alreadyUnlocked_throwsAdminServiceException() {
        ChangeUserLockDto dto = new ChangeUserLockDto("user@acme.com", "UNLOCK");
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));

        AdminServiceException ex = assertThrows(AdminServiceException.class,
                () -> service.unlockUser(dto, adminUser));
        assertEquals("User already unlocked!", ex.getMessage());
    }

    // --- increaseFailedAttempts ---

    @Test
    void increaseFailedAttempts_incrementsAndSaves() {
        regularUser.setFailedAttempts(2);

        service.increaseFailedAttempts(regularUser);

        assertEquals(3, regularUser.getFailedAttempts());
        verify(userRepository).save(regularUser);
    }

    // --- bruteForceLockUser ---

    @Test
    void bruteForceLockUser_locksUserAndSaves() {
        service.bruteForceLockUser(regularUser);

        assertFalse(regularUser.isAccountNonLocked());
        verify(userRepository).save(regularUser);
    }

    // --- resetFailedAttempts ---

    @Test
    void resetFailedAttempts_userWithAttempts_resetsToZero() {
        regularUser.setFailedAttempts(3);
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));

        service.resetFailedAttempts("user@acme.com");

        assertEquals(0, regularUser.getFailedAttempts());
        verify(userRepository).save(regularUser);
    }

    @Test
    void resetFailedAttempts_userWithZeroAttempts_doesNotSave() {
        regularUser.setFailedAttempts(0);
        when(userRepository.findByUsernameIgnoreCase("user@acme.com")).thenReturn(Optional.of(regularUser));

        service.resetFailedAttempts("user@acme.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetFailedAttempts_userNotFound_doesNothing() {
        when(userRepository.findByUsernameIgnoreCase("nobody@acme.com")).thenReturn(Optional.empty());

        service.resetFailedAttempts("nobody@acme.com");

        verify(userRepository, never()).save(any());
    }

    // --- hasAdminRole ---

    @Test
    void hasAdminRole_userHasAdminRole_returnsTrue() {
        assertTrue(service.hasAdminRole(adminUser));
    }

    @Test
    void hasAdminRole_userDoesNotHaveAdminRole_returnsFalse() {
        assertFalse(service.hasAdminRole(regularUser));
    }
}
