package KTB4_gourmet_Week6.Assignment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDto {

    @NotBlank(message = "email is required")
    @Email(message = "email format is invalid")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}