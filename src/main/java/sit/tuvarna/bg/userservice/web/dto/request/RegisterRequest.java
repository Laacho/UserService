package sit.tuvarna.bg.userservice.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Second name is required")
    private String secondName;
    @NotBlank(message = "Third name is required")
    private String thirdName;
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Egn is required")
    private String egn;
    @Past(message = "Birth date must be in the past")
    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @Email
    private String email;
}
