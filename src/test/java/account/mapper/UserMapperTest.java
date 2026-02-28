package account.mapper;

import account.domain.dto.AddUserDto;
import account.domain.dto.UserResponseDto;
import account.domain.entities.Group;
import account.domain.entities.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void addUserDtoToEntity_convertsCorrectly() {
        AddUserDto dto = new AddUserDto("John", "Doe", "JOHN@acme.com", "password123");
        User user = mapper.addUserDtoToEntity(dto);

        assertEquals("John", user.getName());
        assertEquals("Doe", user.getLastname());
        assertEquals("john@acme.com", user.getEmail());
        assertEquals("john@acme.com", user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void toUserResponseDto_singleRole_convertsCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setName("Jane");
        user.setLastname("Smith");
        user.setEmail("jane@acme.com");
        Group group = new Group("ROLE_USER", "business");
        user.setGroups(new ArrayList<>(List.of(group)));

        UserResponseDto dto = mapper.toUserResponseDto(user);

        assertEquals(1L, dto.id());
        assertEquals("Jane", dto.name());
        assertEquals("Smith", dto.lastname());
        assertEquals("jane@acme.com", dto.email());
        assertEquals(List.of("ROLE_USER"), dto.roles());
    }

    @Test
    void toUserResponseDto_multipleRoles_rolesSorted() {
        User user = new User();
        user.setId(2L);
        user.setName("Bob");
        user.setLastname("Brown");
        user.setEmail("bob@acme.com");
        Group group1 = new Group("ROLE_USER", "business");
        Group group2 = new Group("ROLE_ACCOUNTANT", "business");
        user.setGroups(new ArrayList<>(List.of(group1, group2)));

        UserResponseDto dto = mapper.toUserResponseDto(user);

        assertEquals(List.of("ROLE_ACCOUNTANT", "ROLE_USER"), dto.roles());
    }
}
