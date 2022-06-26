package pathfinder.prodo.prodoserver.transaction.cardIssueHistory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.card.CardRepository;
import pathfinder.prodo.prodoserver.card.Dto.GetCardIssueRes;
import pathfinder.prodo.prodoserver.card.VO.Card;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardIssueTxService {
    private final CardIssueTxRepository cardIssueTxRepository;
    private final CardRepository cardRepository;

    @Transactional
    public void save(CardIssueTx cardIssueTx) {
        cardIssueTxRepository.save(cardIssueTx);
    }

    public void createCardIssueTx(Long cardId, Long userId) {
        CardIssueTx cardIssueTx = CardIssueTx.builder()
                .cardId(cardId)
                .ownerId(userId)
                .updatedAt(LocalDateTime.now())
                .build();
        save(cardIssueTx);
    }

    public List<GetCardIssueRes> getCardIssueHistory(Long userId, int pageId) {
        List<GetCardIssueRes> getCardIssueResList = new ArrayList<>();
        List<CardIssueTx> cardIssueTxList = cardIssueTxRepository.getCardIssueHistory(userId, PageRequest.of(pageId, 20)).orElse(null);
        for (int i = 0; i < cardIssueTxList.size(); i++) {
            Card card = cardRepository.getById(cardIssueTxList.get(i).getCardId());
            getCardIssueResList.add(
                    new GetCardIssueRes(
                            card.getCardId(),
                            card.getTitle(),
                            card.getImgUrl(),
                            card.getCreatedAt()
                    )
            );
        }
        return getCardIssueResList;
    }
}
