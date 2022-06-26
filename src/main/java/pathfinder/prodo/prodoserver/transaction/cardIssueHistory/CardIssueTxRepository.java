package pathfinder.prodo.prodoserver.transaction.cardIssueHistory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardIssueTxRepository extends JpaRepository<CardIssueTx, Long> {

    @Query(value = "select c from CardIssueTx c where c.ownerId = :userId order by c.updatedAt DESC ")
    Optional<List<CardIssueTx>> getCardIssueHistory(Long userId, Pageable limit);
}
