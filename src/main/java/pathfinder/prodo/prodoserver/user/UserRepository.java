package pathfinder.prodo.prodoserver.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pathfinder.prodo.prodoserver.user.VO.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query(value = "select u from User u where u.nickname = :nickname")
    Optional<User> findByNickname(String nickname);

    boolean existsByAccountAddress(String accountAddress);

    @Query(value = "select u from User u where u.accountAddress = :accountAddress")
    Optional<User> findByAccountAddress(String accountAddress);
}
