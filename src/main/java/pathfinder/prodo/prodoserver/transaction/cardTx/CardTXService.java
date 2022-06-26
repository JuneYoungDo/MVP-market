package pathfinder.prodo.prodoserver.transaction.cardTx;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.transaction.cardTx.dto.GetCardTxRes;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardTXService {
    private final CardTXRepository cardTXRepository;
    private final UserRepository userRepository;

    @Transactional
    public void save(CardTX cardTX) {
        cardTXRepository.save(cardTX);
    }

    public void createCardTX(Long cardId,Long ownerId) {
        CardTX cardTX = CardTX.builder()
                .cardId(cardId)
                .ownerId(ownerId)
                .updatedAt(LocalDateTime.now())
                .build();
        save(cardTX);
    }

    public List<GetCardTxRes> getCardTx(Long cardId) {
        List<CardTX> cardTXList = cardTXRepository.getCardTx(cardId).orElse(null);
        List<GetCardTxRes> getCardTxRes = new ArrayList<>();
        for(int i=0;i<cardTXList.size();i++) {
            User user = userRepository.getById(cardTXList.get(i).getOwnerId());
            getCardTxRes.add(new GetCardTxRes(
                    user.getUserId(),
                    user.getPhotoUrl(),
                    user.getNickname(),
                    user.getAccountAddress(),
                    cardTXList.get(i).getUpdatedAt()
                    )
            );
        }
        return getCardTxRes;
    }
}
