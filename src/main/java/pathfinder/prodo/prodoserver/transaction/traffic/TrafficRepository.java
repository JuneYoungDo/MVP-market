package pathfinder.prodo.prodoserver.transaction.traffic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrafficRepository extends JpaRepository<AiTraffic, Long> {

    @Query(value = "select count(t) from AiTraffic t where t.finished = false")
    Optional<Long> countTraffic();
}
