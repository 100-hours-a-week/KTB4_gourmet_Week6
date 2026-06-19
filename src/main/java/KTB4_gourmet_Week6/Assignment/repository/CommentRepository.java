package KTB4_gourmet_Week6.Assignment.repository;

import KTB4_gourmet_Week6.Assignment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_IdOrderByIdAsc(Long postId);

    void deleteByPost_Id(Long postId);

    void deleteByUser_Id(Long userId);
}