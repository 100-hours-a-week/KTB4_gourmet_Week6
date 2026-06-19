package KTB4_gourmet_Week6.Assignment.controller;

import KTB4_gourmet_Week6.Assignment.dto.PostCreateRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.PostResponseDto;
import KTB4_gourmet_Week6.Assignment.dto.PostUpdateRequestDto;
import KTB4_gourmet_Week6.Assignment.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/users/{userId}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDto createPost(
            @PathVariable Long userId,
            @Valid @RequestBody PostCreateRequestDto request
    ) {
        return postService.createPost(userId, request);
    }

    @GetMapping("/posts")
    public List<PostResponseDto> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postService.getPosts(page, size);
    }

    @GetMapping("/posts/{postId}")
    public PostResponseDto getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

    @PatchMapping("/posts/{postId}")
    public PostResponseDto updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto request
    ) {
        return postService.updatePost(postId, request);
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }
}