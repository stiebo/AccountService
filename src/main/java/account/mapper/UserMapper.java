package account.mapper;

import account.domain.dto.AddUserDto;
import account.domain.dto.UserResponseDto;
import account.domain.entities.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public User addUserDtoToEntity (AddUserDto dto) {
        return new User()
                .setName(dto.getName())
                .setLastname(dto.getLastname())
                .setEmail(dto.getEmail().toLowerCase())
                .setUsername(dto.getEmail().toLowerCase())
                .setPassword(dto.getPassword());
    }

    public UserResponseDto toUserResponseDto(User user) {
        List<String> roles = new ArrayList<>();
        user.getGroups().forEach(group -> roles.add(group.getName()));
        Collections.sort(roles);
        return new UserResponseDto(
                user.getId(), user.getName(),
                user.getLastname(), user.getEmail(),
                roles);
    }
}
