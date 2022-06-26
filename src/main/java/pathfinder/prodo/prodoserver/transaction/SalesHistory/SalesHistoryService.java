package pathfinder.prodo.prodoserver.transaction.SalesHistory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.card.CardRepository;
import pathfinder.prodo.prodoserver.user.Dto.GetSalesHistoryRes;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.UtilService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesHistoryService {
    private final SalesHistoryRepository salesHistoryRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UtilService utilService;

    @Transactional
    public void save(SalesHistory salesHistory) {
        salesHistoryRepository.save(salesHistory);
    }

    public void createSalesHistory(Long sellerId, Long buyerId, Long cardId, double pay) {
        SalesHistory salesHistory = SalesHistory.builder()
                .sellerId(sellerId)
                .buyerId(buyerId)
                .cardId(cardId)
                .pay(pay)
                .updatedAt(LocalDateTime.now())
                .build();
        save(salesHistory);
    }

    public List<GetSalesHistoryRes> getSalesHistory(Long userId, int pageId) {
        List<GetSalesHistoryRes> getSalesHistoryResList = new ArrayList<>();
        List<SalesHistory> salesHistoryList = salesHistoryRepository.getSalesHistory(userId, PageRequest.of(pageId,20))
                .orElse(null);
        for (int i = 0; i < salesHistoryList.size(); i++) {
            SalesHistory salesHistory = salesHistoryList.get(i);
            if (salesHistory.getSellerId() == userId) {  // 본인이 판매 한 경우
                User buyer = userRepository.getById(salesHistory.getBuyerId());
                getSalesHistoryResList.add(
                        new GetSalesHistoryRes(
                                "Sell",
                                buyer.getUserId(),
                                buyer.getNickname(),
                                salesHistory.getCardId(),
                                cardRepository.getById(salesHistory.getCardId()).getImgUrl(),
                                cardRepository.getById(salesHistory.getCardId()).getTitle(),
                                utilService.doubleToString(salesHistory.getPay()),
                                salesHistory.getUpdatedAt()
                        )
                );
            } else {    // 본인이 구매 한 경우
                User seller = userRepository.getById(salesHistory.getSellerId());
                getSalesHistoryResList.add(
                        new GetSalesHistoryRes(
                                "Buy",
                                seller.getUserId(),
                                seller.getNickname(),
                                salesHistory.getCardId(),
                                cardRepository.getById(salesHistory.getCardId()).getImgUrl(),
                                cardRepository.getById(salesHistory.getCardId()).getTitle(),
                                utilService.doubleToString(salesHistory.getPay()),
                                salesHistory.getUpdatedAt()
                        )
                );
            }
        }
        return getSalesHistoryResList;
    }
}
