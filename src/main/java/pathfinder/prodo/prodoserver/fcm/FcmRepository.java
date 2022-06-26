package pathfinder.prodo.prodoserver.fcm;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Long> {

    @Query(value = "select f from Fcm f where f.userId = :userId order by f.updatedAt DESC")
    Optional<List<Fcm>> getFcmHistory(Long userId, Pageable limit);
}
