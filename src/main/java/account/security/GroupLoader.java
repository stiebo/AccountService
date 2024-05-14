package account.security;

import account.domain.entities.Group;
import account.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupLoader {
    private final GroupRepository repository;

    @Autowired
    public GroupLoader(GroupRepository groupRepository) {
        this.repository = groupRepository;
        createRoles();
    }

    private void createRoles() throws RuntimeException {
        try {
            repository.save(new Group("ROLE_ADMINISTRATOR", "admin"));
            repository.save(new Group("ROLE_USER", "business"));
            repository.save(new Group("ROLE_ACCOUNTANT", "business"));
            repository.save(new Group("ROLE_AUDITOR", "business"));
        } catch (Exception e) {

        }
    }
}
