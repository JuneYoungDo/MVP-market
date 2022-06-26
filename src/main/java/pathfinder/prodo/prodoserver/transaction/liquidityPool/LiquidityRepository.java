package pathfinder.prodo.prodoserver.transaction.liquidityPool;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidityRepository extends JpaRepository<Liquidity, Long> {

    @Query(value = "select l from Liquidity l where l.userId = :userId")
    Optional<List<Liquidity>> getLiquidity(Long userId);

    @Query(value = "select l from Liquidity l")
    Optional<List<Liquidity>> getAllLiquidity();

}
