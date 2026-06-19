package KTB4_gourmet_Week6.Assignment.service;

import KTB4_gourmet_Week6.Assignment.dto.PostCreateRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.PostResponseDto;
import KTB4_gourmet_Week6.Assignment.dto.PostUpdateRequestDto;
import KTB4_gourmet_Week6.Assignment.entity.Post;
import KTB4_gourmet_Week6.Assignment.entity.User;
import KTB4_gourmet_Week6.Assignment.exception.PostNotFoundException;
import KTB4_gourmet_Week6.Assignment.exception.UserNotFoundException;
import KTB4_gourmet_Week6.Assignment.repository.CommentRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostImageRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostLikeRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostRepository;
import KTB4_gourmet_Week6.Assignment.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImageRepository postImageRepository;

    @Transactional
    public PostResponseDto createPost(Long userId, PostCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));

        Post post = new Post(
                user,
                request.getTitle(),
                request.getContent()
        );

        Post savedPost = postRepository.save(post);

        return new PostResponseDto(savedPost);
    }

    public List<PostResponseDto> getPosts(int page, int size) {
        return postRepository.findAll(
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                )
                .getContent()
                .stream()
                .map(PostResponseDto::new)
                .toList();
    }

    public PostResponseDto getPost(Long postId) {
        Post post = findPostById(postId);

        return new PostResponseDto(post);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto request) {
        Post post = findPostById(postId);

        post.update(
                request.getTitle(),
                request.getContent()
        );

        return new PostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);

        postImageRepository.deleteByPost_Id(postId);
        postLikeRepository.deleteByPost_Id(postId);
        commentRepository.deleteByPost_Id(postId);

        postRepository.delete(post);
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));
    }
}