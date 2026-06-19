/*
package KTB4_gourmet_Week6.Assignment.repository;

import KTB4_gourmet_Week6.Assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}*/

package KTB4_gourmet_Week6.Assignment.repository;

import KTB4_gourmet_Week6.Assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndPassword(String email, String password);

    List<User> findAllByOrderByIdAsc();

    //추후 닉네임 중복 검사 때 사용.
    // boolean existsByNickname(String nickname);
}