package pathfinder.prodo.prodoserver.card;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pathfinder.prodo.prodoserver.card.VO.Card;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(value = "select count(c) from Card c")
    Optional<Long> countAllCards();

    @Query(value = "select c from Card c where c.id = :id")
    Optional<Card> getByNftId(String id);


    @Query(value = "select c from Card c where c.deleted = false and c.user.userId = :userId order by c.createdAt DESC")
    Optional<List<Card>> getCards(Long userId, Pageable limit);
}
