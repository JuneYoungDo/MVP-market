package pathfinder.prodo.prodoserver.transaction.cardTx;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardTXRepository extends JpaRepository<CardTX, Long> {

    @Query(value = "select c from CardTX c where c.cardId = :cardId order by c.updatedAt DESC ")
    Optional<List<CardTX>> getCardTx(Long cardId);
}
