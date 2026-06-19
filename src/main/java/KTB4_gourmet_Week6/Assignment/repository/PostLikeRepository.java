package KTB4_gourmet_Week6.Assignment.repository;

import KTB4_gourmet_Week6.Assignment.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    //boolean existsByUser_IdAndPost_Id(Long userId, Long postId); 이미지 좋아요 눌렀는지 확인용

    //Optional<PostLike> findByUser_IdAndPost_Id(Long userId, Long postId); 누른 좋아요 취소 용도

    //long countByPost_Id(Long postId); 좋아요 갯수 용도

    void deleteByPost_Id(Long postId);

    void deleteByUser_Id(Long userId);
}