package account.controller;

import account.business.AdminService;
import account.business.EventName;
import account.business.SecurityService;
import account.domain.dto.ChangeUserLockDto;
import account.domain.dto.ChangeUserRoleDto;
import account.domain.dto.UserResponseDto;
import account.mapper.UserMapper;
import account.domain.entities.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/admin")
public class AdminController {
    private final AdminService service;
    private final UserMapper mapper;
    private final SecurityService logger;

    @Autowired
    public AdminController(AdminService adminService, UserMapper userMapper, SecurityService securityService) {
        this.service = adminService;
        this.mapper = userMapper;
        this.logger = securityService;
    }


    @PutMapping("/user/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@Valid @RequestBody ChangeUserRoleDto changeUserRoleDto,
                                                          @AuthenticationPrincipal User requester) {

        // do you need the switch?
        switch (changeUserRoleDto.operation()) {
            case "GRANT":
                User user = service.grantRole("ROLE_" + changeUserRoleDto.role(),
                        changeUserRoleDto.user(), requester);
                logger.logEvent(EventName.GRANT_ROLE,
                        requester.getUsername(),
                        "Grant role %s to %s".formatted(changeUserRoleDto.role(), user.getUsername()),
                        "/api/admin/user/role");
                return ResponseEntity.ok(mapper.toUserResponseDto(user));
            case "REMOVE":
                user = service.removeRole("ROLE_" + changeUserRoleDto.role(),
                        changeUserRoleDto.user(), requester);
                logger.logEvent(EventName.REMOVE_ROLE,
                        requester.getUsername(),
                        "Remove role %s from %s".formatted(changeUserRoleDto.role(), user.getUsername()),
                        "/api/admin/user/role");
                return ResponseEntity.ok(mapper.toUserResponseDto(user));
            default: return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/user/access")
    public ResponseEntity<Map<String, String>> changeUserLock(@Valid @RequestBody ChangeUserLockDto changeUserLockDto,
                                                              @AuthenticationPrincipal User requester) {
        switch (changeUserLockDto.operation()) {
            case "LOCK":
                service.lockUser(changeUserLockDto, requester);
                logger.logEvent(EventName.LOCK_USER,
                        requester.getUsername(),
                        "Lock user " + changeUserLockDto.user().toLowerCase(),
                        "api/admin/user/access");
                return ResponseEntity.ok(Map.of(
                        "status",
                        "User %s %s!".formatted(changeUserLockDto.user().toLowerCase(), "locked")));
            case "UNLOCK":
                service.unlockUser(changeUserLockDto, requester);
                logger.logEvent(EventName.UNLOCK_USER,
                        requester.getUsername(),
                        "Unlock user " + changeUserLockDto.user().toLowerCase(),
                        "api/admin/user/access");
                return ResponseEntity.ok(Map.of(
                        "status",
                        "User %s %s!".formatted(changeUserLockDto.user().toLowerCase(), "unlocked")));
            default: return ResponseEntity.badRequest().build();
        }
    }

    // Better to return a 204 here
    @DeleteMapping("/user/{user_email}")
    public ResponseEntity<?> deleteUser(@PathVariable("user_email") String userEmail,
                                        @AuthenticationPrincipal User requester) {
        service.deleteUser(userEmail, requester);
        logger.logEvent(EventName.DELETE_USER,
                requester.getUsername(),
                userEmail,
                "api/admin/user");
        return ResponseEntity.ok(Map.of("user", userEmail, "status", "Deleted successfully!"));
    }

    @GetMapping("/user/")
    public ResponseEntity<UserResponseDto[]> getAllUsers() {
        List<User> users = service.getAllUsers();
        return ResponseEntity.ok(users.stream()
                .map(mapper::toUserResponseDto)
                .toArray(UserResponseDto[]::new));
    }

}
