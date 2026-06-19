package KTB4_gourmet_Week6.Assignment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PostUpdateRequestDto {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "content is required")
    private String content;
}