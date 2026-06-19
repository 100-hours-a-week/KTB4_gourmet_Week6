package KTB4_gourmet_Week6.Assignment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserUpdateRequestDto {

    @NotBlank(message = "nickname is required")
    private String nickname;
}