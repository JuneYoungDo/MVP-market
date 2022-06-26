package pathfinder.prodo.prodoserver.market;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.card.CardRepository;
import pathfinder.prodo.prodoserver.card.VO.Card;
import pathfinder.prodo.prodoserver.coin.CoinService;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.market.Dto.*;
import pathfinder.prodo.prodoserver.market.VO.Market;
import pathfinder.prodo.prodoserver.market.VO.SuspicionMarket;
import pathfinder.prodo.prodoserver.transaction.cardTx.CardTXService;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.UserService;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.Bcrypt;
import pathfinder.prodo.prodoserver.utils.Secret;
import pathfinder.prodo.prodoserver.utils.UtilService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pathfinder.prodo.prodoserver.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class MarketService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MarketRepository marketRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final SuspicionMarketRepository suspicionMarketRepository;
    private final UtilService utilService;
    private final UserService userService;
    private final CardTXService cardTXService;
    private final Bcrypt bcrypt;
    private final CoinService coinService;

    @Transactional
    public void save(Market market) {
        marketRepository.save(market);
    }

    public void uploadMarket(Long userId, UploadMarketReq uploadMarketReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        if (user.getPayPwd() == null || user.getPayPwd().equals("")) throw new BaseException(EMPTY_PAY_PWD);
        if (!bcrypt.isMatch(uploadMarketReq.getPayPwd(), user.getPayPwd())) throw new BaseException(MISS_MATCH_PWD);

        if (user.getMarketCount() > 0) user.setMarketCount(user.getMarketCount() - 1);
        else coinService.sendFeeDalle(userId, 1);

        Card card = cardRepository.getById(uploadMarketReq.getCardId());
        if (card.getUser() != user)
            throw new BaseException(BaseResponseStatus.IS_NOT_YOUR_CARD);
        List<Market> markets = card.getMarkets();
        boolean isExisted = false;
        if (markets.size() != 0 || markets != null) {
            for (int i = 0; i < markets.size(); i++) {
                if (markets.get(i).isDeleted() == false)
                    isExisted = true;
            }
        }
        if (isExisted)
            throw new BaseException(BaseResponseStatus.ALREADY_IN_MARKET);
        Market market = Market.builder()
                .pay(uploadMarketReq.getPay())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        market.setUser(user);
        market.setCard(card);
        save(market);
    }

    public boolean isExistMarket(Long marketId) {
        if (marketRepository.existsById(marketId) == false) return false;
        Market market = marketRepository.getById(marketId);
        if (market.isDeleted() == true) return false;
        return true;
    }

    public List<MainFeedRes> getMainFeed(Long userId, int pageId) {
        List<MainFeedRes> mainFeedList = new ArrayList<>();
        List<Market> markets = marketRepository.getMarketsSortWithTime(PageRequest.of(pageId, 20)).orElse(null);
        if (userId == null) {
            for (int i = 0; i < markets.size(); i++) {
                Market market = markets.get(i);
                mainFeedList.add(new MainFeedRes(market.getMarketId(), market.getCard().getImgUrl(), market.getCard().getTitle(),
                        market.getUser().getNickname(), market.getUser().getPhotoUrl(), utilService.doubleToString(market.getPay())));
            }
        } else {
            User user = userRepository.getById(userId);
            List<Card> cardHideList = user.getHideCards();
            for (int i = 0; i < markets.size(); i++) {
                Market market = markets.get(i);
                Card card = market.getCard();
                if (cardHideList.contains(card))
                    continue;
                mainFeedList.add(new MainFeedRes(market.getMarketId(), market.getCard().getImgUrl(), market.getCard().getTitle(),
                        market.getUser().getNickname(), market.getUser().getPhotoUrl(), utilService.doubleToString(market.getPay())));
            }
        }

        return mainFeedList;
    }

    public MarketRes getMarket(Long userId, Long marketId) throws BaseException {
        if (marketRepository.existsById(marketId) == false)
            throw new BaseException(BaseResponseStatus.INVALID_MARKET_ID);
        User user = null;
        if (userId != null)
            user = userRepository.getById(userId);
        Market market = marketRepository.getById(marketId);
        Card card = market.getCard();
        User owner = market.getUser();
        boolean isMine = false;
        if (user == owner) isMine = true;
        String type = "Mindalle";
        if (card.getType() != null && card.getType().equals("Normal")) type = "Normal";
        return new MarketRes(
                market.getMarketId(),
                Secret.NFT_CONTRACTS_ACCOUNT,
                card.getCardId(),
                card.getId(),
                card.getImgUrl(),
                card.getTitle(),
                card.getDescription(),
                card.getTxHash(),
                utilService.doubleToString(market.getPay()),
                owner.getUserId(),
                owner.getNickname(),
                owner.getPhotoUrl(),
                market.getCreatedAt(),
                isMine,
                type,
                cardTXService.getCardTx(card.getCardId())
        );
    }


    @Transactional
    public void reportMarket(Long userId, ReportMarketReq reportMarketReq) throws BaseException {
        if (isExistMarket(reportMarketReq.getMarketId()) == false) throw new BaseException(NOT_EXIST_MARKET);
        if (reportMarketReq.getDescription().length() == 0) throw new BaseException(NOT_ENOUGH_LENGTH);
        SuspicionMarket suspicionMarket = SuspicionMarket.builder()
                .reportUserId(userId)
                .suspicionMarketId(reportMarketReq.getMarketId())
                .description(reportMarketReq.getDescription())
                .build();
        suspicionMarketRepository.save(suspicionMarket);
        if (suspicionMarketRepository.countReport(reportMarketReq.getMarketId()).orElse(0L) >= 5) {
            Market market = marketRepository.getById(reportMarketReq.getMarketId());
            market.setDeleted(true);
        }
    }

    @Transactional
    public void deleteMarket(Long userId, DeleteMarketReq deleteMarketReq) throws BaseException {
        if (!isExistMarket(deleteMarketReq.getMarketId())) throw new BaseException(INVALID_MARKET_ID);
        User user = userRepository.getById(userId);
        Market market = marketRepository.getById(deleteMarketReq.getMarketId());
        if (!user.getMarkets().contains(market)) throw new BaseException(IS_NOT_YOUR_MARKET);
        market.setDeleted(true);
    }
}
