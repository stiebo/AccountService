package account.business;

import account.domain.dto.ChangeUserLockDto;
import account.domain.entities.User;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    int MAX_FAILED_ATTEMPTS = 5;
    List<User> getAllUsers();
    Optional<User> getUserByUsername(String username);
    void deleteUser(String username, User requester);
    User grantRole(String role, String username, User requester);
    User removeRole(String role, String username, User requester);
    void lockUser(ChangeUserLockDto changeUserLockDto, User requester);
    void unlockUser(ChangeUserLockDto changeUserLockDto, User requester);
    void increaseFailedAttempts(User user);
    void resetFailedAttempts(String username);
    boolean hasAdminRole(User user);
    void bruteForceLockUser(User user);
}
