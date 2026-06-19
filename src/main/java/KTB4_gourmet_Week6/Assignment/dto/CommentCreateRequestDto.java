package KTB4_gourmet_Week6.Assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CommentCreateRequestDto {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "content is required")
    private String content;
}