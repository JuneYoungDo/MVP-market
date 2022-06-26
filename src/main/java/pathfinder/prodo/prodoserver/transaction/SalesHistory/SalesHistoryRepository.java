package pathfinder.prodo.prodoserver.transaction.SalesHistory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesHistoryRepository extends JpaRepository<SalesHistory, Long> {

    @Query(value = "select s from SalesHistory s where s.sellerId = :userId or s.buyerId = :userId order by s.updatedAt DESC ")
    Optional<List<SalesHistory>> getSalesHistory(Long userId, Pageable limit);
}
