package pathfinder.prodo.prodoserver.transaction.coinTx;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoinTxRepository extends JpaRepository<CoinTx,Long> {
}
