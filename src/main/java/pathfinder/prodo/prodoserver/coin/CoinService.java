package pathfinder.prodo.prodoserver.coin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.card.CardRepository;
import pathfinder.prodo.prodoserver.card.VO.Card;
import pathfinder.prodo.prodoserver.coin.Dto.*;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.fcm.FcmService;
import pathfinder.prodo.prodoserver.market.MarketRepository;
import pathfinder.prodo.prodoserver.market.VO.Market;
import pathfinder.prodo.prodoserver.transaction.SalesHistory.SalesHistoryService;
import pathfinder.prodo.prodoserver.transaction.accountTx.AccountTXService;
import pathfinder.prodo.prodoserver.transaction.cardTx.CardTXService;
import pathfinder.prodo.prodoserver.transaction.coinTx.CoinTxService;
import pathfinder.prodo.prodoserver.transaction.liquidityPool.LiquidityService;
import pathfinder.prodo.prodoserver.transaction.liquidityPool.dto.GetLpInfo;
import pathfinder.prodo.prodoserver.transaction.transaction.TransactionService;
import pathfinder.prodo.prodoserver.user.Dto.BuyCardReq;
import pathfinder.prodo.prodoserver.user.Dto.SendKlayReq;
import pathfinder.prodo.prodoserver.user.Dto.SendNftOutReq;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.Secret;
import pathfinder.prodo.prodoserver.utils.UtilService;
import pathfinder.prodo.prodoserver.utils.mail.MailService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.Map;

import static pathfinder.prodo.prodoserver.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CoinService {
    private final UserRepository userRepository;
    private final UtilService utilService;
    private final CoinCountRepository coinCountRepository;
    private final TransactionService transactionService;
    private final AccountTXService accountTXService;
    private final CoinTxService coinTxService;
    private final CardRepository cardRepository;
    private final CardTXService cardTXService;
    private final MarketRepository marketRepository;
    private final FcmService fcmService;
    private final SalesHistoryService salesHistoryService;
    private final LiquidityService liquidityService;
    private final MailService mailService;

    // 빗썸 입,출금 가능 여부 확인하기
    public boolean checkBiThumb() throws IOException, InterruptedException, ParseException, BaseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri((URI.create("https://api.bithumb.com/public/assetsstatus/klay")))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_BITHUMB);
        JSONObject jsonObject = utilService.parseBody(response);
        Map<String, Object> map = new ObjectMapper().readValue(jsonObject.get("data").toString(), Map.class);
        if ((Integer) map.get("deposit_status") == 1 && (Integer) map.get("withdrawal_status") == 1) return true;
        else return false;
    }

    // 빗썸 클레이 시세 확인하기
    public String checkKlayPrice() throws IOException, InterruptedException, BaseException, ParseException {
        if (!checkBiThumb()) throw new BaseException(FAILED_TO_GET_BITHUMB);
        HttpRequest request = HttpRequest.newBuilder()
                .uri((URI.create("https://api.bithumb.com/public/ticker/klay_KRW")))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_MARKET_PRICE);
        JSONObject jsonObject = utilService.parseBody(response);
        Map<String, Object> map = new ObjectMapper().readValue(jsonObject.get("data").toString(), Map.class);
        return (String) map.get("closing_price");
    }

    public GetDalleCountRes getAllDalleCount() {
        return new GetDalleCountRes(coinCountRepository.getDaliCount().orElse("0"));
    }

    // 1달리 시세 확인하기(KLAY)
    public double getDaliMarketPrice() throws BaseException, IOException, ParseException, InterruptedException {
        DecimalFormat format = new DecimalFormat();
        format.applyLocalizedPattern("0.000000");
        String nowDaliCount = coinCountRepository.getDaliCount().orElse("0");
        double nowDaliPrice = 200d + (Double.parseDouble(nowDaliCount) * Double.parseDouble(nowDaliCount) / 50000000000d);
        double ceilNowDaliPrice = Math.ceil(nowDaliPrice * 1000000) / 1000000.0;
        double klayPrice = Double.parseDouble(checkKlayPrice());
        double marketPrice = Math.ceil(ceilNowDaliPrice / klayPrice * 1000000) / 1000000.0;

        return marketPrice;
    }

    public double dalleForLpCoin(double klay) throws BaseException, IOException, ParseException, InterruptedException {
        double nowKlay = utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL);
        double nowDalle = getCoinByAddress(Secret.LP_ACCOUNT_TVL);
        double dallePerKlay = nowDalle - (nowKlay * nowDalle) / (nowKlay + 1);
        return Math.ceil(dallePerKlay * klay * 1000000) / 1000000.0;
    }

    public double klayForReturnLp(double dalle) throws BaseException, IOException, ParseException, InterruptedException {
        double nowKlay = utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL);
        double nowDalle = getCoinByAddress(Secret.LP_ACCOUNT_TVL);
        double klay = nowKlay - (nowKlay * nowDalle) / (nowDalle + 1);
        return Math.ceil(klay * dalle * 1000000) / 1000000.0;
    }

    public double swapKlayToDalle(double klay) throws BaseException, IOException, ParseException, InterruptedException {
        double nowKlay = utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL);
        double nowDalle = getCoinByAddress(Secret.LP_ACCOUNT_TVL);
        double dalle = nowDalle - (nowKlay * nowDalle) / (nowKlay + klay);
        return Math.floor(dalle * 1000000) / 1000000.0;
    }

    public double swapDalleToKlay(double dalle) throws BaseException, IOException, ParseException, InterruptedException {
        double nowKlay = utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL);
        double nowDalle = getCoinByAddress(Secret.LP_ACCOUNT_TVL);
        double klay = nowKlay - (nowKlay * nowDalle) / (nowDalle + dalle);
        return Math.floor(klay * 1000000) / 1000000.0;
    }

    // 수수료 계산하기
    public double getFeeForKlayToDali(double klay) {
        double fee = Math.ceil(klay * 0.005 * 1000000) / 1000000.0;
        return fee;
    }

    // 순수 전달 목적의 KLAY 전송
    public String sendKlay(String senderAccount, String receiverAccount, double pay) throws IOException, InterruptedException, ParseException, BaseException {
        String hexPay = utilService.decimalToHex(pay);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wallet-api.klaytnapi.com/v2/tx/fd-user/value"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString(
                        "{\n  \"from\": \"" + senderAccount + "\",\n  \"value\": \"" + hexPay + "\",\n  \"to\": \"" + receiverAccount + "\",\n  \"memo\": \"0x123\",\n  \"nonce\": 0,\n  \"gas\": 0,\n  \"submit\": true,\n  \"feePayer\": \"" + Secret.FEE_PAYER_ACCOUNT + "\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.out.println(response);
            throw new BaseException(FAILED_TO_SEND_KLAY);
        }
        JSONObject jsonObject = utilService.parseBody(response);
        String txHash = (String) jsonObject.get("transactionHash");
        return txHash;
    }

    // 순수 전달 목적 NFT 전송
    public String sendNftCard(String senderAccount, String receiverAccount, String id) throws IOException, InterruptedException, BaseException, ParseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip17-api.klaytnapi.com/v1/contract/" + Secret.NFT_CONTRACTS + "/token/0x" + id))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("x-krn", "")
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n  \"sender\": \"" + senderAccount + "\",\n  \"owner\": \"" + senderAccount + "\",\n  \"to\": \"" + receiverAccount + "\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_SEND_NFT);
        JSONObject jsonObject = utilService.parseBody(response);
        String txHash = (String) jsonObject.get("transactionHash");
        return txHash;
    }

    // 순수 전달 목적 COIN 전송
    public String sendCoin(String senderAccount, String receiverAccount, double amount) throws IOException, InterruptedException, BaseException, ParseException {
        String hexAmount = utilService.decimalToHex(amount);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.FT_CONTRACTS + "/transfer"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n  \"from\": \"" + senderAccount + "\",\n  \"to\": \"" + receiverAccount + "\",\n  \"amount\": \"" + hexAmount + "\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response);
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_SEND_COIN);
        JSONObject jsonObject = utilService.parseBody(response);
        String txHash = (String) jsonObject.get("transactionHash");
        return txHash;
    }

    // 순수 전달 목적 LP 전송
    public String sendLP(String senderAccount, String receiverAccount, double amount) throws IOException, InterruptedException, BaseException, ParseException {
        String hexAmount = utilService.decimalToHex(amount);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.LP_CONTRACTS + "/transfer"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n  \"from\": \"" + senderAccount + "\",\n  \"to\": \"" + receiverAccount + "\",\n  \"amount\": \"" + hexAmount + "\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            mailService.sendAllUsersEmail("ehwnsdud1004@naver.com", "error", response.statusCode() + " body : " + response.body() +" 양 : "+ hexAmount);
            throw new BaseException(FAILED_TO_SEND_COIN);
        }
        JSONObject jsonObject = utilService.parseBody(response);
        String txHash = (String) jsonObject.get("transactionHash");
        return txHash;
    }

    // 클레이로 달리 스왑하기(클레이)
    public void klayToDaliForKlay(Long userId, ExchangeKlayToDali2Req exchangeKlayToDali2Req) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkPwd(user, exchangeKlayToDali2Req.getPayPwd());                      // 비밀번호 확인
        utilService.checkTransactionCommit(userId);                                         // 최근 트랜잭션 커밋 확인
        double klay = exchangeKlayToDali2Req.getKlay();                                     // 교환을 원하는 클레이
        if (Double.parseDouble(utilService.getKlay(userId).getKlay()) < klay) {
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_KLAY);
        }
        double fee = getFeeForKlayToDali(klay);                                             // 수수료
        double dalle = swapKlayToDalle(klay - fee);
        System.out.println(dalle);
        if (getCoinByAddress(Secret.LP_ACCOUNT_TVL) < dalle) {
            throw new BaseException(FAILED_SWAP_NOT_ENOUGH_DALLE);
        }
        String feeTx = sendKlay(user.getAccountAddress(), Secret.FEE_KLAY_ACCOUNT, fee);
        String txHash = sendKlay(user.getAccountAddress(), Secret.LP_ACCOUNT_TVL, klay - fee);
        transactionService.createTx(userId, null, txHash);
        String txHash2 = sendCoin(Secret.LP_ACCOUNT_TVL, user.getAccountAddress(), dalle);
        transactionService.createTx(null, userId, txHash2);
        accountTXService.createdAccountTx("swap1", klay, dalle, 0, userId, null, null);
    }

    // 달리로 클레이 스왑하기(달리)
    @Transactional
    public void daliToKlayForDali(Long userId, ExchangeKlayToDaliReq exchangeKlayToDaliReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkPwd(user, exchangeKlayToDaliReq.getPayPwd());
        utilService.checkTransactionCommit(userId);
        double dali = exchangeKlayToDaliReq.getDali();                              // 교환을 원하는 달리 양
        if (Double.parseDouble(getCoin(userId)) < dali) {
            throw new BaseException(NOT_ENOUGH_DALI);
        }
        double feeDali = getFeeForKlayToDali(dali);                                 // 수수료
        double klay = swapDalleToKlay(dali - feeDali);
        if (utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL) < klay) {
            throw new BaseException(FAILED_SWAP_NOT_ENOUGH_KLAY);
        }
        // 코인 전송
        String txHh2 = sendCoin(user.getAccountAddress(), Secret.FEE_COIN_ACCOUNT, feeDali);
        String txHh = sendCoin(user.getAccountAddress(), Secret.LP_ACCOUNT_TVL, dali - feeDali);
        transactionService.createTx(userId, null, txHh);
        transactionService.createTx(userId, null, txHh2);
        // 클레이 지급
        String txHash = sendKlay(Secret.LP_ACCOUNT_TVL, user.getAccountAddress(), klay);
        transactionService.createTx(null, userId, txHash);
        accountTXService.createdAccountTx("swap2", dali, klay, 0, userId, null, null);
    }

    // 내 보유 코인 갯수 확인하기
    public String getCoin(Long userId) throws IOException, InterruptedException, BaseException, ParseException {
        User user = userRepository.getById(userId);
        String account = user.getAccountAddress();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.FT_CONTRACTS + "/account/" + account + "/balance"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_COIN);
        JSONObject jsonObject = utilService.parseBody(response);
        String tmp = (String) jsonObject.get("balance");
        if (tmp == null) throw new BaseException(FAILED_TO_GET_COIN);
        return utilService.hexToPeb(tmp);
    }

    public double getCoinByAddress(String address) throws IOException, InterruptedException, BaseException, ParseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.FT_CONTRACTS + "/account/" + address + "/balance"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_COIN);
        JSONObject jsonObject = utilService.parseBody(response);
        String tmp = (String) jsonObject.get("balance");
        if (tmp == null) throw new BaseException(FAILED_TO_GET_COIN);
        return Double.parseDouble(utilService.hexToPeb(tmp));
    }

    // 내 보유 LP 갯수 확인하기
    public String getLp(Long userId) throws IOException, InterruptedException, BaseException, ParseException {
        User user = userRepository.getById(userId);
        String account = user.getAccountAddress();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.LP_CONTRACTS + "/account/" + account + "/balance"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_COIN);
        JSONObject jsonObject = utilService.parseBody(response);
        String tmp = (String) jsonObject.get("balance");
        if (tmp == null) throw new BaseException(FAILED_TO_GET_COIN);
        return utilService.hexToPeb(tmp);
    }

    // 코인 발급하기
    @Transactional
    public void issueCoin(Long userId, double amount) throws IOException, InterruptedException, BaseException, ParseException {
        if (Double.parseDouble(coinCountRepository.getDaliCount().orElse("0")) + amount >= 30000000d)
            throw new BaseException(MAXIMUM_DALI);
        String account = userRepository.getById(userId).getAccountAddress();
        String hexAmount = utilService.decimalToHex(amount);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.FT_CONTRACTS + "/mint"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n  \"from\": \"\",\n  \"to\": \"" + account + "\",\n  \"amount\": \"" + hexAmount + "\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_MAKE_COIN);

        coinTxService.createCoinTx(userId, amount, "make");
    }

    public void issueLpCoin(Long userId, double amount) throws BaseException, IOException, InterruptedException {
        String account = userRepository.getById(userId).getAccountAddress();
        String hexAmount = utilService.decimalToHex(amount);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://kip7-api.klaytnapi.com/v1/contract/" + Secret.LP_CONTRACTS + "/mint"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString("{\n  \"from\": \"\",\n  \"to\": \"" + account + "\",\n  \"amount\": \"" + hexAmount + "\"\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_MAKE_COIN);

        coinTxService.createCoinTx(userId, amount, "LP_make");
    }

    // 클레이 전송
    public void sendKlayControl(Long userId, SendKlayReq sendKlayReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkPwd(user, sendKlayReq.getPayPwd());
        utilService.checkTransactionCommit(userId);
        if (Double.parseDouble(utilService.getKlay(userId).getKlay()) < sendKlayReq.getPay() + 0.01)
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_KLAY);

        String feeTx = sendKlay(user.getAccountAddress(), Secret.FEE_KLAY_ACCOUNT, 0.01);
        transactionService.createTx(userId, null, feeTx);
        String txHash = sendKlay(user.getAccountAddress(), sendKlayReq.getReceiverAccountAddress(), sendKlayReq.getPay());

        if (userRepository.existsByAccountAddress(sendKlayReq.getReceiverAccountAddress())) {
            User receiver = userRepository.findByAccountAddress(sendKlayReq.getReceiverAccountAddress()).orElse(null);
            transactionService.createTx(userId, receiver.getUserId(), txHash);
            accountTXService.createdAccountTx("klayOut", sendKlayReq.getPay() + 0.01, 0, 0, userId, receiver.getUserId(), null);
        } else {
            transactionService.createTx(userId, null, txHash);
            accountTXService.createdAccountTx("klayOut", sendKlayReq.getPay() + 0.01, 0, 0, userId, null, sendKlayReq.getReceiverAccountAddress());
        }
    }

    // NFT 전송
    @Transactional
    public void sendNftControl(Long userId, SendNftOutReq sendNftOutReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        Long cardId = sendNftOutReq.getCardId();
        String senderAccount = user.getAccountAddress();
        String receiverAccount = sendNftOutReq.getReceiverAccount();
        String id = cardRepository.getById(cardId).getId();
        if (!cardRepository.existsById(cardId) || cardRepository.getById(cardId).isDeleted() == true)
            throw new BaseException(INVALID_CARD_ID);
        if (!user.getCards().contains(cardRepository.getById(cardId))) throw new BaseException(IS_NOT_YOUR_CARD);
        utilService.checkPwd(user, sendNftOutReq.getPayPwd());
        utilService.checkTransactionCommit(userId);
        if (Double.parseDouble(utilService.getKlay(userId).getKlay()) < 1)      // 1클레이 수수료 있는지 확인
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_KLAY);

        String feeTx = sendKlay(senderAccount, Secret.FEE_KLAY_ACCOUNT, 1);
        transactionService.createTx(userId, null, feeTx);
        accountTXService.createdAccountTx("NftOut", 1, 0, 0, userId, null, receiverAccount);
        String txHash = sendNftCard(senderAccount, receiverAccount, id);
        if (userRepository.existsByAccountAddress(sendNftOutReq.getReceiverAccount())) {        // 내부 계좌
            User receiver = userRepository.findByAccountAddress(sendNftOutReq.getReceiverAccount()).orElse(null);
            transactionService.createTx(userId, receiver.getUserId(), txHash);              // 양방향 트랜잭션
            Card card = cardRepository.getByNftId(id).orElse(null);
            card.setUser(receiver);                                                         // 카드 소유권 변경
            cardTXService.createCardTX(card.getCardId(), userId);                           // 카드 트랜잭션
        } else {                                                                            // 외부 계좌
            Card card = cardRepository.getById(cardId);
            card.setDeleted(true);
            transactionService.createTx(userId, null, txHash);
        }
    }

    @Transactional
    public void buyNftCardByDalle(Long userId, BuyCardReq buyCardReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        Market market = marketRepository.getById(buyCardReq.getMarketId());
        utilService.checkPwd(user, buyCardReq.getPayPwd());
        utilService.checkTransactionCommit(userId);
        if (market.getUser() == user) throw new BaseException(IS_YOUR_CARD);

        User seller = market.getUser();
        double totalPay = market.getPay();
        if (Double.parseDouble(getCoin(userId)) < totalPay + 0.1)
            throw new BaseException(NOT_ENOUGH_DALI);
        double feeDalle = Math.ceil(totalPay * 0.05 * 1000000) / 1000000.0;
        double userPay = ((totalPay - feeDalle) * 1000000) / 1000000.0;
        String txHash = sendCoin(user.getAccountAddress(), seller.getAccountAddress(), userPay);        // 95% 지불
        transactionService.createTx(userId, seller.getUserId(), txHash);
        String txHash2 = sendCoin(user.getAccountAddress(), Secret.FEE_COIN_ACCOUNT, feeDalle + 0.1);    // 5% 지불 + 0.1 수수료
        transactionService.createTx(userId, null, txHash2);
        accountTXService.createdAccountTx("market", totalPay + 0.1, userPay, 0, userId, seller.getUserId(), null);

        String txHash3 = sendNftCard(seller.getAccountAddress(), user.getAccountAddress(), market.getCard().getId());
        transactionService.createTx(seller.getUserId(), userId, txHash3);
        cardTXService.createCardTX(market.getCard().getCardId(), userId);

        Card card = market.getCard();
        card.setUser(user);
        market.setDeleted(true);

        salesHistoryService.createSalesHistory(seller.getUserId(), userId, card.getCardId(), market.getPay());
        Long fcmId = fcmService.createFcm("카드가 판매되었습니다", seller.getUserId(),
                null, null, null, null, card.getCardId());
        fcmService.sendMessageTo(seller.getDeviceToken(), fcmId, null, "카드가 판매되었습니다!",
                card.getTitle() + " 카드가 판매되었습니다!");
    }

    public void sendFeeDalle(Long userId, double fee) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkTransactionCommit(userId);
        if (Double.parseDouble(getCoin(userId)) < fee) throw new BaseException(NOT_ENOUGH_DALI);
        String txHash = sendCoin(user.getAccountAddress(), Secret.FEE_COIN_ACCOUNT, fee);
        transactionService.createTx(userId, null, txHash);
        String type;
        if (fee == 3) type = "mindalle";
        else type = "upload";
        accountTXService.createdAccountTx(type, fee, 0, 0, userId, null, null);
    }

    public void sendFeeNormal(Long userId, double fee) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkTransactionCommit(userId);
        if (Double.parseDouble(getCoin(userId)) < fee) throw new BaseException(NOT_ENOUGH_DALI);
        String txHash = sendCoin(user.getAccountAddress(), Secret.FEE_COIN_ACCOUNT, fee);
        transactionService.createTx(userId, null, txHash);
        accountTXService.createdAccountTx("Normal", fee, 0, 0, userId, null, null);
    }

    public void sendDalleInUser(Long userId, SendDalleReq sendDalleReq) throws BaseException, IOException, ParseException, InterruptedException {
        User sender = userRepository.getById(userId);
        if (!userRepository.existsByAccountAddress(sendDalleReq.getReceiverAccountAddress()))
            throw new BaseException(INVALID_ACCOUNT_FOR_DALLE);
        User receiver = userRepository.findByAccountAddress(sendDalleReq.getReceiverAccountAddress()).orElse(null);
        if (receiver.isDeleted()) throw new BaseException(INVALID_ACCOUNT_FOR_DALLE);
        utilService.checkPwd(sender, sendDalleReq.getPayPwd());
        utilService.checkTransactionCommit(userId);
        if (Double.parseDouble(getCoin(userId)) < sendDalleReq.getDalle() + 0.1)
            throw new BaseException(NOT_ENOUGH_DALI);
        String feeTx = sendCoin(sender.getAccountAddress(), Secret.FEE_COIN_ACCOUNT, 0.1);
        transactionService.createTx(userId, null, feeTx);
        String txHash = sendCoin(sender.getAccountAddress(), receiver.getAccountAddress(), sendDalleReq.getDalle());
        transactionService.createTx(userId, receiver.getUserId(), txHash);
        accountTXService.createdAccountTx("dalle", sendDalleReq.getDalle() + 0.1, sendDalleReq.getDalle(), 0, userId, receiver.getUserId(), null);
    }

    @Transactional
    public void issueLpToken(Long userId, ExchangeLpReq exchangeLpReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkPwd(user, exchangeLpReq.getPayPwd());                      // 비밀번호 확인
        utilService.checkTransactionCommit(userId);                                   // 최근 트랜잭션 커밋 확인

        double lp = exchangeLpReq.getAmount();
        double allLp = liquidityService.getAllLp();
        double percent = Math.ceil(lp / allLp * 1000000) / 1000000.0;
        double dalle = Math.ceil(getCoinByAddress(Secret.LP_ACCOUNT_TVL) * percent * 1000000) / 1000000.0;
        double klay = Math.ceil(utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL) * percent * 1000000) / 1000000.0;

        if (Double.parseDouble(getCoin(userId)) < dalle) throw new BaseException(NOT_ENOUGH_DALI);
        if (Double.parseDouble(utilService.getKlay(userId).getKlay()) < klay + 0.01)
            throw new BaseException(NOT_ENOUGH_KLAY);

        String feeTx = sendKlay(user.getAccountAddress(), Secret.FEE_KLAY_ACCOUNT, 0.01);
        transactionService.createTx(userId, null, feeTx);
        String txHash1 = sendKlay(user.getAccountAddress(), Secret.LP_ACCOUNT_TVL, klay);
        transactionService.createTx(userId, null, txHash1);
        String txHash2 = sendCoin(user.getAccountAddress(), Secret.LP_ACCOUNT_TVL, dalle);
        transactionService.createTx(userId, null, txHash2);
        issueLpCoin(userId, lp);
        liquidityService.create(userId, "In", lp, klay, dalle);
        accountTXService.createdAccountTx("LpSwap1", klay, dalle, lp, userId, null, null);
    }

    @Transactional
    public void returnLpToken(Long userId, ExchangeLpReq exchangeLpReq) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        utilService.checkPwd(user, exchangeLpReq.getPayPwd());                      // 비밀번호 확인
        utilService.checkTransactionCommit(userId);                                   // 최근 트랜잭션 커밋 확인

        double userLp = Double.parseDouble(getLp(userId));
        double amount = exchangeLpReq.getAmount();
        if (userLp < amount)
            throw new BaseException(NOT_ENOUGH_LP);

        double allLp = liquidityService.getAllLp();
        double percent = Math.floor(amount / allLp * 1000000) / 1000000.0;
        double klay = Math.floor(utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL) * percent * 1000000) / 1000000.0;

        if (klay <= 0.01) throw new BaseException(NOT_ENOUGH_LP);
        String feeTx = sendKlay(Secret.LP_ACCOUNT_TVL, Secret.FEE_KLAY_ACCOUNT, 0.01);

        klay = klay - 0.01;
        double dalle = Math.floor(getCoinByAddress(Secret.LP_ACCOUNT_TVL) * percent * 1000000) / 1000000.0;

        String txHash3 = sendLP(user.getAccountAddress(), Secret.LP_ACCOUNT_TVL, amount);
        transactionService.createTx(userId, null, txHash3);

        String txHash1 = sendKlay(Secret.LP_ACCOUNT_TVL, user.getAccountAddress(), klay);
        transactionService.createTx(null, userId, txHash1);
        String txHash2 = sendCoin(Secret.LP_ACCOUNT_TVL, user.getAccountAddress(), dalle);
        transactionService.createTx(null, userId, txHash2);
        liquidityService.create(userId, "Out", amount, klay + 0.01, dalle);
        accountTXService.createdAccountTx("LpSwap2", klay, dalle, amount, null, userId, null);
    }

    public GetLpWithOtherCoin getLpWithOtherCoin(Long userId) throws BaseException, IOException, ParseException, InterruptedException {
        return new GetLpWithOtherCoin(getLp(userId));
    }

    public GetTvlRes getTvlWithLpCount() throws BaseException, IOException, ParseException, InterruptedException {
        double dalle = getCoinByAddress(Secret.LP_ACCOUNT_TVL);
        double klay = utilService.getKlayFromAddress(Secret.LP_ACCOUNT_TVL);
        double tvl = Math.ceil(klay * 2 * Double.parseDouble(checkKlayPrice()) * 1000000) / 1000000.0;
        double allLp = liquidityService.getAllLp();
        return new GetTvlRes(utilService.doubleToString(tvl), utilService.doubleToString(Math.floor(dalle * 1000000) / 1000000.0),
                utilService.doubleToString(klay), utilService.doubleToString(allLp));
    }
}
