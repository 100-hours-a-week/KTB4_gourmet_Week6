package KTB4_gourmet_Week6.Assignment.service;

import KTB4_gourmet_Week6.Assignment.dto.CommentCreateRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.CommentResponseDto;
import KTB4_gourmet_Week6.Assignment.dto.CommentUpdateRequestDto;
import KTB4_gourmet_Week6.Assignment.entity.Comment;
import KTB4_gourmet_Week6.Assignment.entity.Post;
import KTB4_gourmet_Week6.Assignment.entity.User;
import KTB4_gourmet_Week6.Assignment.exception.CommentNotFoundException;
import KTB4_gourmet_Week6.Assignment.exception.PostNotFoundException;
import KTB4_gourmet_Week6.Assignment.exception.UserNotFoundException;
import KTB4_gourmet_Week6.Assignment.repository.CommentRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostRepository;
import KTB4_gourmet_Week6.Assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, CommentCreateRequestDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));

        Comment comment = new Comment(
                post,
                user,
                request.getContent()
        );

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDto(savedComment);
    }

    public List<CommentResponseDto> getComments(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        return commentRepository.findByPost_IdOrderByIdAsc(postId)
                .stream()
                .map(CommentResponseDto::new)
                .toList();
    }

    public CommentResponseDto getComment(Long postId, Long commentId) {
        Comment comment = findCommentById(commentId);

        validateCommentBelongsToPost(comment, postId);

        return new CommentResponseDto(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(
            Long postId,
            Long commentId,
            CommentUpdateRequestDto request
    ) {
        Comment comment = findCommentById(commentId);

        validateCommentBelongsToPost(comment, postId);

        comment.update(request.getContent());

        return new CommentResponseDto(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = findCommentById(commentId);

        validateCommentBelongsToPost(comment, postId);

        commentRepository.delete(comment);
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));
    }

    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPostId().equals(postId)) {
            throw new CommentNotFoundException("해당 게시글의 댓글을 찾을 수 없습니다.");
        }
    }
}