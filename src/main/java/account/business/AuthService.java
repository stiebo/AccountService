package account.business;

import account.domain.entities.User;

public interface AuthService {
    User registerUser (User user);
    void changePassword (User user, String newPassword);
}
