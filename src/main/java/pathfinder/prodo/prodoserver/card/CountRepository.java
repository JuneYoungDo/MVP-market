package pathfinder.prodo.prodoserver.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pathfinder.prodo.prodoserver.card.VO.Count;

import java.util.Optional;

@Repository
public interface CountRepository extends JpaRepository<Count, Long> {

    @Query(value = "select c.cardCount from count c where c.countId = :countId")
    Optional<Long> getCardCount(Long countId);

}
