package pathfinder.prodo.prodoserver.coin;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pathfinder.prodo.prodoserver.coin.Dto.*;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponse;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.market.MarketService;
import pathfinder.prodo.prodoserver.user.Dto.BuyCardReq;
import pathfinder.prodo.prodoserver.user.Dto.SendKlayReq;
import pathfinder.prodo.prodoserver.user.Dto.SendNftOutReq;
import pathfinder.prodo.prodoserver.utils.JwtService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;
    private final JwtService jwtService;
    private final MarketService marketService;

    @GetMapping("/test/coin")
    public ResponseEntity test(@RequestBody ExchangeKlayToDaliReq exchangeKlayToDaliReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.daliToKlayForDali(userId, exchangeKlayToDaliReq);
            return new ResponseEntity(coinService.getCoin(userId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SEND_KLAY), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SEND_KLAY.getStatus()));
        }
    }

    /**
     * 1달리 시세 확인하기[KLAY]
     * [GET] /dali/price
     */
    @GetMapping("/dali/price")
    public ResponseEntity getDaliPrice() {
        try {
            return new ResponseEntity(new GetDaliPriceRes(coinService.getDaliMarketPrice()), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_GET_MARKET_PRICE), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_GET_MARKET_PRICE.getStatus()));
        }
    }

    /**
     * 클레이를 달리로 환전하기(클레이)
     * [POST] /klay/dali/2
     */
    @PostMapping("/klay/dali/2")
    public ResponseEntity exchangeKlayToDali2(@RequestBody ExchangeKlayToDali2Req exchangeKlayToDali2Req) {
        try {
            Long userId = jwtService.getUserId();
            coinService.klayToDaliForKlay(userId, exchangeKlayToDali2Req);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | ParseException | InterruptedException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SWAP), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SWAP.getStatus()));
        }
    }

    /**
     * 달리를 클레이로 환전하기(달리)
     * [POST] /dali/klay
     */
    @PostMapping("/dali/klay")
    public ResponseEntity exchangeDaliToKlay(@RequestBody ExchangeKlayToDaliReq exchangeKlayToDaliReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.daliToKlayForDali(userId, exchangeKlayToDaliReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | ParseException | InterruptedException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SWAP), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SWAP.getStatus()));
        }
    }

    /**
     * 클레이 외부 전송하기
     * [POST] /user/klay
     */
    @PostMapping("/user/klay")
    public ResponseEntity sendKlay(@RequestBody SendKlayReq sendKlayReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.sendKlayControl(userId, sendKlayReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SEND_KLAY), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SEND_KLAY.getStatus()));
        }
    }

    /**
     * NFT 외부 전송하기
     * [POST] /user/card/out
     */
    @PostMapping("/user/card/out")
    public ResponseEntity sendNftOut(@RequestBody SendNftOutReq sendNftOutReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.sendNftControl(userId, sendNftOutReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SEND_NFT), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SEND_NFT.getStatus()));
        }
    }

    /**
     * NFT 구매하기 (클레이 전달하기 -> NFT 전달하기)
     * [POST] /user/klay/nft
     */
    @PostMapping("/user/klay/nft")
    public ResponseEntity buyNftWithKlay(@RequestBody BuyCardReq buyCardReq) {
        try {
            Long userId = jwtService.getUserId();
            if (!marketService.isExistMarket(buyCardReq.getMarketId()))
                return new ResponseEntity(new BaseResponse(BaseResponseStatus.INVALID_MARKET_ID),
                        HttpStatus.valueOf(BaseResponseStatus.INVALID_NFT_ID.getStatus()));
            coinService.buyNftCardByDalle(userId, buyCardReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_BUY_NFT), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_BUY_NFT.getStatus()));
        }
    }

    /**
     * DallE 전송하기(내부 계좌만)
     * [POST] /user/dalle/out
     */
    @PostMapping("/user/dalle/out")
    public ResponseEntity sendDalleInUser(@RequestBody SendDalleReq sendDalleReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.sendDalleInUser(userId, sendDalleReq);
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
     * DallE 현재 풀린 양 가져오기
     * [GET] /all/dalle
     */
    @GetMapping("/all/dalle")
    public ResponseEntity getDalleCount() {
        return new ResponseEntity(coinService.getAllDalleCount(), HttpStatus.valueOf(200));
    }

    /**
     * LP Token 발급받기
     * [POST] /lp/token
     */
    @PostMapping("/lp/token")
    public ResponseEntity issueLpToken(@RequestBody ExchangeLpReq issueLpTokenReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.issueLpToken(userId, issueLpTokenReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SWAP), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SWAP.getStatus()));
        }
    }

    /**
     * LP Token -> 클레이 + 달리
     * [POST] /lp/token/return
     */
    @PostMapping("/lp/token/return")
    public ResponseEntity returnLpToken(@RequestBody ExchangeLpReq exchangeLpReq) {
        try {
            Long userId = jwtService.getUserId();
            coinService.returnLpToken(userId, exchangeLpReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_SWAP), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_SWAP.getStatus()));
        }
    }

    /**
     * 보유한 LP토큰과 맡겨져 있는 Klay, Dalle 양 보기
     * [GET] /lp/klay/dalle
     */
    @GetMapping("/lp/klay/dalle")
    public ResponseEntity getLpWithCoin() {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(coinService.getLpWithOtherCoin(userId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()),
                    HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_GET_MARKET_PRICE), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_GET_MARKET_PRICE.getStatus()));
        }
    }

    /**
     * 전체 TVL 보기
     * [GET] /tvl
     */
    @GetMapping("/tvl")
    public ResponseEntity getTVL() {
        try {
            return new ResponseEntity(coinService.getTvlWithLpCount(), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_GET_MARKET_PRICE), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_GET_MARKET_PRICE.getStatus()));
        }
    }


}