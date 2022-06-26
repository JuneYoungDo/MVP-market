package pathfinder.prodo.prodoserver.transaction.transaction;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "select t from Transaction t where t.senderId = :userId or t.receiverId = :userId order by t.createdAt DESC")
    Optional<List<Transaction>> getRecentlyTx(Long userId, Pageable limit);
}
