package account.business.impl;

import account.business.AdminService;
import account.domain.dto.ChangeUserLockDto;
import account.business.exception.AdminServiceException;
import account.business.exception.RoleNotFoundException;
import account.business.exception.UserNotFoundException;
import account.domain.entities.Group;
import account.domain.entities.User;
import account.repository.GroupRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(String username, User requester) throws UserNotFoundException {
        checkAndThrowIfSelf(username, requester, "Can't remove ADMINISTRATOR!");
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public User grantRole(String role, String username, User requester) {
        User user = findUserOrThrow(username);
        checkAndThrowIfInvalidRole(role);
        checkAndThrowIfRoleCategoryViolation(role, user);
        checkAndThrowIfSelf(username, requester, "Can't change ADMINISTRATOR role!");
        checkAndThrowIfAlreadyHasRole(role, user);
        Group group = groupRepository.findByName(role);
        user.addGroup(group);
        userRepository.save(user);
        return user;
    }

    @Override
    @Transactional
    public User removeRole(String role, String username, User requester) {
        User user = findUserOrThrow(username);
        checkAndThrowIfInvalidRole(role);
        Group group = findGroupOrThrowIfNotAUserRole(role, user);
        checkAndThrowIfSelf(username, requester, "Can't remove ADMINISTRATOR role!");
        checkAndThrowIfLastRole(user);
        user.removeGroup(group);
        userRepository.save(user);
        return user;
    }

    @Override
    @Transactional
    public void lockUser(ChangeUserLockDto changeUserLockDto, User requester) throws AdminServiceException {
        User user = findUserOrThrow(changeUserLockDto.user());
        checkAndThrowIfSelf(changeUserLockDto.user(), requester, "Can't lock the ADMINISTRATOR!");
        if (user.isAccountNonLocked()) {
            user.setAccountNonLocked(false);
        } else {
            throw new AdminServiceException("User already locked!");
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockUser(ChangeUserLockDto changeUserLockDto, User requester) throws AdminServiceException {
        User user = findUserOrThrow(changeUserLockDto.user());
        if (!user.isAccountNonLocked()) {
            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
        } else {
            throw new AdminServiceException("User already unlocked!");
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void increaseFailedAttempts(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void bruteForceLockUser(User user) {
        user.setAccountNonLocked(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetFailedAttempts(String username) {
        Optional<User> optionalUser = getUserByUsername(username);
        if (optionalUser.isPresent()) {
            if (optionalUser.get().getFailedAttempts() != 0) {
                optionalUser.get().setFailedAttempts(0);
                userRepository.save(optionalUser.get());
            }
        }
    }

    @Override
    public boolean hasAdminRole(User user) {
        List<Group> groups = user.getGroups();
        for (Group group : groups) {
            if (group.getName().equals("ROLE_ADMINISTRATOR")) {
                return true;
            }
        }
        return false;
    }

    private User findUserOrThrow(String username) throws UserNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(UserNotFoundException::new);
    }

    private void checkAndThrowIfSelf(String username, User requester, String message) throws AdminServiceException {
        if (username.equalsIgnoreCase(requester.getUsername())) {
            throw new AdminServiceException(message);
        }
    }

    private void checkAndThrowIfInvalidRole(String role) throws RoleNotFoundException {
        List<Group> groups = groupRepository.findAll();
        groups.stream()
                .filter(group -> group.getName().equals(role))
                .findAny()
                .orElseThrow(RoleNotFoundException::new);
    }

    private Group findGroupOrThrowIfNotAUserRole(String role, User user) throws AdminServiceException {
        List<Group> groups = user.getGroups();
        return groups.stream()
                .filter(group -> group.getName().equals(role))
                .findAny()
                .orElseThrow(() -> new AdminServiceException("The user does not have this role!"));
    }

    private void checkAndThrowIfRoleCategoryViolation(String role, User user) throws AdminServiceException {
        String userRoleCategory = user.getGroups().get(0).getCategory();
        String roleCategory = groupRepository.findByName(role).getCategory();
        if (!userRoleCategory.equals(roleCategory)) {
            throw new AdminServiceException("The user cannot combine administrative and business roles!");
        }
    }

    private void checkAndThrowIfLastRole(User user) throws AdminServiceException {
        if (user.getGroups().size() <= 1) {
            throw new AdminServiceException("The user must have at least one role!");
        }
    }

    private void checkAndThrowIfAlreadyHasRole(String role, User user) throws AdminServiceException {
        List<Group> groups = user.getGroups();
        for (Group group : groups) {
            if (group.getName().equals(role)) {
                throw new AdminServiceException("The user already has a role!");
            }
        }
    }

}
