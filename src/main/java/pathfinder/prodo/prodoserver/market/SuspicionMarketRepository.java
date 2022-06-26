package pathfinder.prodo.prodoserver.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pathfinder.prodo.prodoserver.market.VO.SuspicionMarket;

import java.util.Optional;

@Repository
public interface SuspicionMarketRepository extends JpaRepository<SuspicionMarket, Long> {

    @Query(value = "select count(m) from suspicion_market m where m.suspicionMarketId=:suspicionMarketId")
    Optional<Long> countReport(Long suspicionMarketId);
}
