package KTB4_gourmet_Week6.Assignment.service;

import KTB4_gourmet_Week6.Assignment.dto.LoginRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.UserResponseDto;
import KTB4_gourmet_Week6.Assignment.dto.UserSignupRequestDto;
import KTB4_gourmet_Week6.Assignment.dto.UserUpdateRequestDto;
import KTB4_gourmet_Week6.Assignment.entity.Post;
import KTB4_gourmet_Week6.Assignment.entity.User;
import KTB4_gourmet_Week6.Assignment.exception.DuplicateEmailException;
import KTB4_gourmet_Week6.Assignment.exception.InvalidLoginException;
import KTB4_gourmet_Week6.Assignment.exception.UserNotFoundException;
import KTB4_gourmet_Week6.Assignment.repository.CommentRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostImageRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostLikeRepository;
import KTB4_gourmet_Week6.Assignment.repository.PostRepository;
import KTB4_gourmet_Week6.Assignment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostImageRepository postImageRepository;

    @Transactional
    public UserResponseDto signup(UserSignupRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        User user = new User(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );

        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    public UserResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmailAndPassword(
                request.getEmail(),
                request.getPassword()
        ).orElseThrow(() -> new InvalidLoginException("이메일 또는 비밀번호가 일치하지 않습니다."));

        return new UserResponseDto(user);
    }

    public List<UserResponseDto> getUsers() {
        return userRepository.findAllByOrderByIdAsc()
                .stream()
                .map(UserResponseDto::new)
                .toList();
    }

    public UserResponseDto getUser(Long userId) {
        User user = findUserById(userId);

        return new UserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUser(Long userId, UserUpdateRequestDto request) {
        User user = findUserById(userId);

        user.update(request.getNickname());

        return new UserResponseDto(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);

        // 1. 이 회원이 다른 게시글에 누른 좋아요 삭제
        postLikeRepository.deleteByUser_Id(userId);

        // 2. 이 회원이 다른 게시글에 작성한 댓글 삭제
        commentRepository.deleteByUser_Id(userId);

        // 3. 이 회원이 작성한 게시글 조회
        List<Post> posts = postRepository.findByUser_IdOrderByIdAsc(userId);

        // 4. 회원이 작성한 게시글에 연결된 이미지, 좋아요, 댓글 삭제 후 게시글 삭제
        for (Post post : posts) {
            postImageRepository.deleteByPost_Id(post.getId());
            postLikeRepository.deleteByPost_Id(post.getId());
            commentRepository.deleteByPost_Id(post.getId());
            postRepository.delete(post);
        }

        // 5. 마지막으로 회원 삭제
        userRepository.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없습니다."));
    }
}