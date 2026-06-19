/*
package KTB4_gourmet_Week6.Assignment.dto;

import lombok.Getter;
import KTB4_gourmet_Week6.Assignment.entity.Post;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long authorId;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
    }
}
*/

package KTB4_gourmet_Week6.Assignment.dto;

import KTB4_gourmet_Week6.Assignment.entity.Post;
import lombok.Getter;

@Getter
public class PostResponseDto {

    private final Long id;
    private final Long userId;
    private final String title;
    private final String content;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.userId = post.getUserId();
        this.title = post.getTitle();
        this.content = post.getContent();
    }
}
