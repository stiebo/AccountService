package account.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddUserDto {
    @NotBlank private String name;
    @NotBlank private String lastname;
    @NotBlank @Pattern(regexp = "\\w+(\\.\\w+)?@acme.com") private String email;
    @NotBlank private String password;
}
