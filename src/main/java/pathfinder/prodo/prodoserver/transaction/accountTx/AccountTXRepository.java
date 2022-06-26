package pathfinder.prodo.prodoserver.transaction.accountTx;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTXRepository extends JpaRepository<AccountTX, Long> {

    @Query(value = "select a from AccountTX a where a.senderId = :userId or a.receiverId = :userId order by a.updatedAt DESC")
    Optional<List<AccountTX>> getAccountTX(Long userId, Pageable limit);
}
