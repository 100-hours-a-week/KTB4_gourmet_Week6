package KTB4_gourmet_Week6.Assignment.repository;

import KTB4_gourmet_Week6.Assignment.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    //List<PostImage> findByPost_IdOrderBySortOrderAsc(Long postId); 게시글 상세 조회 이미지 목록 용도

    void deleteByPost_Id(Long postId);
}