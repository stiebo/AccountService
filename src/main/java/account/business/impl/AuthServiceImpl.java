package account.business.impl;

import account.business.AuthService;
import account.business.exception.PasswordException;
import account.business.exception.UserExistsException;
import account.domain.entities.User;
import account.repository.GroupRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final GroupRepository groupRepository;
    private final List<String> breaches = List.of("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly",
            "PasswordForAugust", "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember",
            "PasswordForDecember");

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
        this.groupRepository = groupRepository;
    }

    @Override
    @Transactional
    public User registerUser(User user) throws UserExistsException, PasswordException {
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new UserExistsException();
        }
        validatePassword(user.getPassword());
        user.setPassword(encoder.encode(user.getPassword()));
        if (userRepository.count() == 0) {
            user.addGroup(groupRepository.findByName("ROLE_ADMINISTRATOR"));
        } else {
            user.addGroup(groupRepository.findByName("ROLE_USER"));
        }
        user.setAccountNonLocked(true)
            .setFailedAttempts(0);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(User user, String newPassword) throws PasswordException {
        validatePassword(newPassword);
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new PasswordException("The passwords must be different!");
        }
        user.setPassword(encoder.encode(newPassword));
        user.setFailedAttempts(0);
        userRepository.save(user);
    }

    private void validatePassword(String newPassword) throws PasswordException {
        if (newPassword.length() < 12) {
            throw new PasswordException("Password length must be 12 chars minimum!");
        }
        if (breaches.contains(newPassword)) {
            throw new PasswordException("The password is in the hacker's database!");
        }
    }
}
