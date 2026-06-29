package frauas.teilr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email.")
    private String email;

    @NotBlank(message = "Username is required.")
    @Size(min = 2, max = 32, message = "Username must be 2–32 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 72, message = "Password must be 6–72 characters.")
    private String password;
}
