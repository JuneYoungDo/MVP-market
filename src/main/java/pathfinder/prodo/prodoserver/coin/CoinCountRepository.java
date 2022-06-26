package pathfinder.prodo.prodoserver.coin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pathfinder.prodo.prodoserver.coin.VO.CoinCount;

import java.util.Optional;

@Repository
public interface CoinCountRepository extends JpaRepository<CoinCount, Long> {

    @Query(value = "select c.daliCount from coin_count c where c.countId = 1")
    Optional<String> getDaliCount();

}
