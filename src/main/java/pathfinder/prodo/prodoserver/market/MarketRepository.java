package pathfinder.prodo.prodoserver.market;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pathfinder.prodo.prodoserver.card.VO.Card;
import pathfinder.prodo.prodoserver.market.VO.Market;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {

    boolean existsByCard(Card card);

    @Query(value = "select m from Market m where m.card = :card")
    Optional<Market> getMarketByCard(Card card);

    @Query(value = "select m from Market m where m.deleted = false and m.card.deleted = false order by m.createdAt DESC")
    Optional<List<Market>> getMarketsSortWithTime(Pageable limit);

    @Query(value = "select m from Market m where m.deleted = false and m.card.deleted = false and m.user.userId = :userId order by m.createdAt DESC ")
    Optional<List<Market>> getMarkets(Long userId, Pageable limit);
}
