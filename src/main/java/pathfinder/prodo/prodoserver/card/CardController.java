package pathfinder.prodo.prodoserver.card;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pathfinder.prodo.prodoserver.card.Dto.NextIdRes;
import pathfinder.prodo.prodoserver.card.Dto.SaveCardReq;
import pathfinder.prodo.prodoserver.coin.CoinService;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponse;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.transaction.cardIssueHistory.CardIssueTxService;
import pathfinder.prodo.prodoserver.transaction.cardTx.CardTXService;
import pathfinder.prodo.prodoserver.utils.JwtService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class CardController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CardService cardService;
    private final CardRepository cardRepository;
    private final CardTXService cardTXService;
    private final JwtService jwtService;
    private final CardIssueTxService cardIssueTxService;
    private final CoinService coinService;

    /**
     * NFT Card 다음 id 내려주기
     * [GET] /card/id
     */
    @GetMapping("/card/id")
    public ResponseEntity nextCardId() {
        return new ResponseEntity(new NextIdRes(cardService.nextCardId()), HttpStatus.valueOf(200));
    }

    /**
     * NFT Card 발급 저장
     * [POST] /card
     */
    @PostMapping("/card")
    public ResponseEntity saveNftCard(@RequestBody SaveCardReq saveCardReq) {
        try {
            Long userId = jwtService.getUserId();
            if (saveCardReq.getType().equals("Normal")) {
                coinService.sendFeeNormal(userId, 1);
            }
            cardService.createCard(userId, saveCardReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SEND_COIN), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SEND_COIN.getStatus()));
        }
    }

    /**
     * NFT Card 상세보기
     * [GET] /card/{cardId}
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity getCard(@PathVariable Long cardId) {
        try {
            if (jwtService.getJwt() == null || jwtService.getJwt() == "") {
                return new ResponseEntity(cardService.getCard(null, cardId), HttpStatus.valueOf(200));
            } else {
                if (!jwtService.verifyJwt(jwtService.getJwt()))
                    return new ResponseEntity(cardService.getCard(null, cardId), HttpStatus.valueOf(200));
                Long userId = jwtService.getUserId();
                return new ResponseEntity(cardService.getCard(userId, cardId), HttpStatus.valueOf(200));
            }
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }

    }

    /**
     * NFT Card TX 확인하기
     * [GET] /card/{cardId}/tx
     */
    @GetMapping("/card/{cardId}/tx")
    public ResponseEntity getCardTx(@PathVariable Long cardId) {
        if (cardRepository.existsById(cardId) == false)
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.INVALID_CARD_ID),
                    HttpStatus.valueOf(BaseResponseStatus.INVALID_CARD_ID.getStatus()));
        return new ResponseEntity(cardTXService.getCardTx(cardId), HttpStatus.valueOf(200));
    }

    /**
     * NFT Card Issue History (발행 내역) 확인하기
     * [GET] /card/issue/history/{pageId}
     */
    @GetMapping("/card/issue/history/{pageId}")
    public ResponseEntity getCardIssueHistory(@PathVariable int pageId) {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(cardIssueTxService.getCardIssueHistory(userId, pageId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


    /**
     * 내 카드 숨김 리스트 보기
     * [GET] /card/hideList
     */
    @GetMapping("/card/hideList")
    public ResponseEntity getHideList() {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(cardService.getHideList(userId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


    /**
     * 해당 카드 숨기기
     * [GET] /card/{cardId}/hide
     */
    @GetMapping("/card/{cardId}/hide")
    public ResponseEntity hideCard(@PathVariable Long cardId) {
        try {
            Long userId = jwtService.getUserId();
            cardService.hideCard(userId, cardId);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 해당 카드 숨기기 해제하기
     * [GET] /card/{cardId}/clear
     */
    @GetMapping("/card/{cardId}/clear")
    public ResponseEntity clearCard(@PathVariable Long cardId) {
        try {
            Long userId = jwtService.getUserId();
            cardService.clearCard(userId, cardId);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }
}
