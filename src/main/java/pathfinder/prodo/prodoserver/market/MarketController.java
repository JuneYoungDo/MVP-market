package pathfinder.prodo.prodoserver.market;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pathfinder.prodo.prodoserver.card.CardService;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponse;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.market.Dto.DeleteMarketReq;
import pathfinder.prodo.prodoserver.market.Dto.ReportMarketReq;
import pathfinder.prodo.prodoserver.market.Dto.UploadMarketReq;
import pathfinder.prodo.prodoserver.utils.JwtService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class MarketController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final MarketService marketService;
    private final CardService cardService;
    private final JwtService jwtService;

    /**
     * 마켓 메인 피드 보기
     * [GET] /main/{pageId}
     */
    @GetMapping("/main/{pageId}")
    public ResponseEntity getMain(@PathVariable int pageId) {
        try {
            if (jwtService.getJwt() == null || jwtService.getJwt().equals(""))
                return new ResponseEntity(marketService.getMainFeed(null, pageId), HttpStatus.valueOf(200));
            else {
                if (!jwtService.verifyJwt(jwtService.getJwt()))
                    return new ResponseEntity(marketService.getMainFeed(null, pageId), HttpStatus.valueOf(200));
                Long userId = jwtService.getUserId();
                return new ResponseEntity(marketService.getMainFeed(userId, pageId), HttpStatus.valueOf(200));
            }
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 마켓 상세 페이지 보기
     * [GET] /main/market/{marketId}
     */
    @GetMapping("/main/market/{marketId}")
    public ResponseEntity getMarket(@PathVariable Long marketId) {
        try {
            if (jwtService.getJwt() == null || jwtService.getJwt() == "") {
                return new ResponseEntity(marketService.getMarket(null, marketId), HttpStatus.valueOf(200));
            } else {
                if (!jwtService.verifyJwt(jwtService.getJwt()))
                    return new ResponseEntity(marketService.getMarket(null, marketId), HttpStatus.valueOf(200));
                Long userId = jwtService.getUserId();
                return new ResponseEntity(marketService.getMarket(userId, marketId), HttpStatus.valueOf(200));
            }
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 마켓에 카드 올리기
     * [POST] /market
     */
    @PostMapping("/market")
    public ResponseEntity uploadMarket(@RequestBody UploadMarketReq uploadMarketReq) {
        try {
            Long userId = jwtService.getUserId();
            // 해당 카드가 유효한 카드인지 확인
            if (!cardService.isValidCardId(uploadMarketReq.getCardId()))
                return new ResponseEntity(new BaseResponse(BaseResponseStatus.INVALID_CARD_ID),
                        HttpStatus.valueOf(BaseResponseStatus.INVALID_CARD_ID.getStatus()));

            marketService.uploadMarket(userId, uploadMarketReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | ParseException | InterruptedException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_ON_SERVER), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_ON_SERVER.getStatus()));
        }
    }

    /**
     * 해당 마켓 삭제하기
     * [DELETE] /market
     */
    @DeleteMapping("/market")
    public ResponseEntity deleteMarket(@RequestBody DeleteMarketReq deleteMarketReq) {
        try {
            Long userId = jwtService.getUserId();
            marketService.deleteMarket(userId, deleteMarketReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


    /**
     * 해당 마켓 신고하기
     * [POST] /market/report
     */
    @PostMapping("/market/report")
    public ResponseEntity reportMarket(@RequestBody ReportMarketReq reportMarketReq) {
        try {
            Long userId = jwtService.getUserId();
            marketService.reportMarket(userId, reportMarketReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

}
