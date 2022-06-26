package pathfinder.prodo.prodoserver.card;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.card.Dto.CardHideListRes;
import pathfinder.prodo.prodoserver.card.Dto.CardRes;
import pathfinder.prodo.prodoserver.card.Dto.SaveCardReq;
import pathfinder.prodo.prodoserver.card.VO.Card;
import pathfinder.prodo.prodoserver.card.VO.Count;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.fcm.Fcm;
import pathfinder.prodo.prodoserver.fcm.FcmRepository;
import pathfinder.prodo.prodoserver.fcm.FcmService;
import pathfinder.prodo.prodoserver.market.VO.Market;
import pathfinder.prodo.prodoserver.transaction.cardIssueHistory.CardIssueTxService;
import pathfinder.prodo.prodoserver.transaction.cardTx.CardTXService;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.Secret;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pathfinder.prodo.prodoserver.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CardService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CountRepository countRepository;
    private final CardTXService cardTXService;
    private final CardIssueTxService cardIssueTxService;
    private final FcmRepository fcmRepository;

    @Transactional
    public void save(Card card) {
        cardRepository.save(card);
    }

    @Transactional
    public void createCard(Long userId, SaveCardReq saveCardReq) {
        User user = userRepository.getById(userId);
        Card card = Card.builder()
                .type(saveCardReq.getType())
                .id(saveCardReq.getId())
                .title(saveCardReq.getTitle())
                .description(saveCardReq.getDescription())
                .TxHash(saveCardReq.getTxHash())
                .imgUrl(saveCardReq.getImgUrl())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .createdBy(user)
                .user(user)
                .markets(null)
                .build();
        save(card);
        cardTXService.createCardTX(card.getCardId(), userId);
        cardIssueTxService.createCardIssueTx(card.getCardId(), userId);
        if (saveCardReq.getFcmId() != 0) {
            Fcm fcm = fcmRepository.getById(saveCardReq.getFcmId());
            fcm.setWatched(true);
        }
    }

    public boolean isValidCardId(Long cardId) {
        if (cardRepository.existsById(cardId) == false) return false;
        Card card = cardRepository.getById(cardId);
        if (card.isDeleted()) return false;
        return true;
    }

    public boolean isValidId(String id) {
        if (cardRepository.getByNftId(id) == null) return false;
        Card card = cardRepository.getByNftId(id).orElse(null);
        if (card.isDeleted()) return false;
        return true;
    }

    public boolean isMyNftId(Long userId, String id) {
        if (!isValidId(id)) return false;
        User user = userRepository.getById(userId);
        Card card = cardRepository.getByNftId(id).orElse(null);
        if (card.getUser() != user) return false;
        return true;
    }

    @Transactional
    public Long nextCardId() {
        Long returnId = countRepository.getCardCount(1L).orElse(null);
        Count count = countRepository.getById(1L);
        count.setCardCount(returnId + 1);
        return returnId;
    }

    public CardRes getCard(Long userId, Long cardId) throws BaseException {
        if (!isValidCardId(cardId)) throw new BaseException(BaseResponseStatus.INVALID_CARD_ID);
        User user = null;
        if (userId != null)
            user = userRepository.getById(userId);
        Card card = cardRepository.getById(cardId);
        List<Market> markets = card.getMarkets();
        boolean isMine = false;
        if (card.getUser() == user) isMine = true;

        boolean isUploaded = false;
        if (markets.size() != 0 || markets != null) {
            for (int i = 0; i < markets.size(); i++) {
                if (markets.get(i).isDeleted() == false)
                    isUploaded = true;
            }
        }
        String type = "Mindalle";
        if (card.getType() != null && card.getType().equals("Normal")) type = "Normal";
        return new CardRes(card.getCardId(),
                Secret.NFT_CONTRACTS_ACCOUNT,
                card.getId(),
                card.getImgUrl(),
                card.getTitle(),
                card.getDescription(),
                card.getTxHash(),
                card.getCreatedAt(),
                isUploaded,
                isMine,
                type,
                cardTXService.getCardTx(cardId));
    }


    public List<CardHideListRes> getHideList(Long userId) {
        User user = userRepository.getById(userId);
        List<Card> cards = user.getHideCards();
        List<CardHideListRes> cardHideResList = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            cardHideResList.add(
                    new CardHideListRes(
                            card.getCardId(),
                            card.getImgUrl(),
                            card.getTitle(),
                            card.getUser().getNickname()
                    )
            );
        }
        return cardHideResList;
    }

    @Transactional
    public void hideCard(Long userId, Long cardId) throws BaseException {
        if (isValidCardId(cardId) == false) throw new BaseException(INVALID_CARD_ID);
        User user = userRepository.getById(userId);
        Card card = cardRepository.getById(cardId);
        if (user.getCards().contains(card)) throw new BaseException(IS_YOUR_CARD);
        if (user.getHideCards().contains(card)) throw new BaseException(ALREADY_BLOCKED_CARD);
        List<Card> blockCardList = user.getHideCards();
        blockCardList.add(card);
        user.setHideCards(blockCardList);
    }

    @Transactional
    public void clearCard(Long userId, Long cardId) throws BaseException {
        if (isValidCardId(cardId) == false) throw new BaseException(INVALID_CARD_ID);
        User user = userRepository.getById(userId);
        Card card = cardRepository.getById(cardId);
        if (!user.getHideCards().contains(card)) throw new BaseException(ALREADY_CLEAR_CARD);
        List<Card> blockCardList = user.getHideCards();
        blockCardList.remove(card);
        user.setHideCards(blockCardList);
    }

}
