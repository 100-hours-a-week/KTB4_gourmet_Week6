package KTB4_gourmet_Week6.Assignment.dto;

import KTB4_gourmet_Week6.Assignment.entity.Comment;
import lombok.Getter;

@Getter
public class CommentResponseDto {

    private final Long id;
    private final Long postId;
    private final Long userId;
    private final String content;

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.postId = comment.getPostId();
        this.userId = comment.getUserId();
        this.content = comment.getContent();
    }
}