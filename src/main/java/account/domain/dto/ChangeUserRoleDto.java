package account.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeUserRoleDto(
        @NotBlank
        String user,
        @NotBlank
        String role,
        @NotBlank
        @Pattern(regexp = "GRANT|REMOVE")
        String operation) {
}
