package pathfinder.prodo.prodoserver.user;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.appManage.VersionRepository;
import pathfinder.prodo.prodoserver.card.CardRepository;
import pathfinder.prodo.prodoserver.card.VO.Card;
import pathfinder.prodo.prodoserver.coin.CoinService;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.fcm.FcmService;
import pathfinder.prodo.prodoserver.market.MarketRepository;
import pathfinder.prodo.prodoserver.market.VO.Market;
import pathfinder.prodo.prodoserver.oauth.KakaoService;
import pathfinder.prodo.prodoserver.transaction.traffic.TrafficRes;
import pathfinder.prodo.prodoserver.transaction.traffic.TrafficService;
import pathfinder.prodo.prodoserver.user.Dto.*;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.Bcrypt;
import pathfinder.prodo.prodoserver.utils.JwtService;
import pathfinder.prodo.prodoserver.utils.Secret;
import pathfinder.prodo.prodoserver.utils.UtilService;
import pathfinder.prodo.prodoserver.utils.mail.MailDto;
import pathfinder.prodo.prodoserver.utils.mail.MailService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pathfinder.prodo.prodoserver.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class UserService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final MarketRepository marketRepository;
    private final FcmService fcmService;
    private final KakaoService kakaoService;
    private final JwtService jwtService;
    private final TrafficService trafficService;
    private final Bcrypt bcrypt;
    private final MailService mailService;
    private final VersionRepository versionRepository;
    private final UtilService utilService;
    private final CoinService coinService;

    public String createAccount() throws IOException, InterruptedException, ParseException, BaseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wallet-api.klaytnapi.com/v2/account"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_CREATE_ACCOUNT);
        JSONObject jsonObject = utilService.parseBody(response);
        if ((String) jsonObject.get("address") == null) throw new BaseException(FAILED_TO_CREATE_ACCOUNT);
        return (String) jsonObject.get("address");
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    public void createUser(String email) throws IOException, ParseException, InterruptedException, BaseException {
        String account = createAccount();
        User user = User.builder()
                .email(email)
                .accountAddress(account)
                .nickname(RandomStringUtils.random(10, true, true))
                .description("")
                .photoUrl("")
                .refreshToken("")
                .deviceToken("")
                .mindalleCount(1L)
                .marketCount(1L)
                .payPwd("")
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        save(user);
    }

    public Long getAppVersion() {
        return versionRepository.getCurrentVersion().orElse(0L);
    }

    public String isUsedEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "free";
        else if (user.isDeleted() == true) return "deleted";
        else return "using";
    }

    @Transactional
    public LoginRes kakaoLogin(String idToken, String deviceToken) throws BaseException, IOException, ParseException, InterruptedException {
        String email = kakaoService.getKakaoUserInfo(idToken);
        String tmp = isUsedEmail(email);
        if (tmp == "free") {     // 회원 가입
            createUser(email);
            User user = userRepository.findByEmail(email).orElse(null);
            user.setDeviceToken(deviceToken);
            user.setRefreshToken(jwtService.createRefreshToken(user.getUserId()));
            return new LoginRes(jwtService.createJwt(user.getUserId()), user.getRefreshToken());
//        } else if(tmp == "deleted") {   // 삭제 처리 회원
        } else {        // 로그인
            User user = userRepository.findByEmail(email).orElse(null);
            user.setDeviceToken(deviceToken);
            user.setRefreshToken(jwtService.createRefreshToken(user.getUserId()));
            return new LoginRes(jwtService.createJwt(user.getUserId()), user.getRefreshToken());
        }
    }

    public LoginRes renewalAccessToken(Long userId, String refreshToken) throws BaseException {
        if (refreshToken.equals("") || refreshToken.length() == 0)
            throw new BaseException(BaseResponseStatus.EMPTY_REFRESH_TOKEN);
        if (!jwtService.verifyRefreshToken(refreshToken)) throw new BaseException(BaseResponseStatus.INVALID_TOKEN);
        else {
            User user = userRepository.getById(userId);
            if (refreshToken.equals(user.getRefreshToken()))
                return new LoginRes(jwtService.createJwt(userId), refreshToken);
            else {
                throw new BaseException(BaseResponseStatus.INVALID_TOKEN);
            }
        }
    }

    public CheckNicknameRes checkNickname(String nickname) {
        User user = userRepository.findByNickname(nickname).orElse(null);
        if (user == null) return new CheckNicknameRes(false);
        else if (user.isDeleted() == true) return new CheckNicknameRes(false);
        else return new CheckNicknameRes(true);
    }

    @Transactional
    public void editUserPage(Long userId, EditUserPageReq editUserPageReq) throws BaseException {
        User user = userRepository.getById(userId);
        if (editUserPageReq.getNickname().equals("") || editUserPageReq.getNickname().length() == 0)
            throw new BaseException(CAN_NOT_BLANK_NICKNAME);
        if (!user.getNickname().equals(editUserPageReq.getNickname())) {
            if (checkNickname(editUserPageReq.getNickname()).isUsed())
                throw new BaseException(IS_USED_NICKNAME);
            user.setNickname(editUserPageReq.getNickname());
        }
        user.setDescription(editUserPageReq.getDescription());
        user.setPhotoUrl(editUserPageReq.getImgUrl());
    }

    @Transactional
    public void editDeviceToken(Long userId, EditDeviceTokenReq editDeviceTokenReq) throws BaseException {
        if (editDeviceTokenReq.getDeviceToken().equals("") || editDeviceTokenReq.getDeviceToken().length() == 0)
            throw new BaseException(CAN_NOT_BLANK_DEVICE_TOKEN);
        User user = userRepository.getById(userId);
        user.setDeviceToken(editDeviceTokenReq.getDeviceToken());
    }

    public boolean isValidUser(Long userId) {
        if (userRepository.existsById(userId) == false) return false;
        User user = userRepository.getById(userId);
        if (user.isDeleted()) return false;
        return true;
    }

    @Transactional
    public TrafficRes checkForMindalle(Long userId) throws BaseException, IOException, ParseException, InterruptedException {
        User user = userRepository.getById(userId);
        if (user.getMindalleCount() > 0) user.setMindalleCount(user.getMindalleCount() - 1);
        else coinService.sendFeeDalle(userId, 3);
        Long trafficNum = trafficService.getCountTraffic();
        if (trafficNum >= 500) throw new BaseException(TOO_MUCH_TRAFFIC);
        Long trafficId = trafficService.createAiTraffic();
        return new TrafficRes(trafficId, trafficNum);
    }

    @Transactional
    public void setPayPwd(Long userId, SetPwdReq setPwdReq) throws BaseException {
        User user = userRepository.getById(userId);
        String pwd = setPwdReq.getPwd();
        if (pwd.length() != 6) throw new BaseException(MISS_MATCH_PWD_LENGTH);
        if (user.getPayPwd() == null || user.getPayPwd().equals("")) user.setPayPwd(bcrypt.encrypt(pwd));
        else throw new BaseException(ALREADY_SET_PWD);
    }

    @Transactional
    public void editPayPwd(Long userId, EditPwdReq editPwdReq) throws BaseException {
        User user = userRepository.getById(userId);
        String nowPwd = editPwdReq.getNowPwd();
        String editPwd = editPwdReq.getEditPwd();
        if (user.getPayPwd() == null || user.getPayPwd().equals("")) throw new BaseException(EMPTY_PAY_PWD);
        if (!bcrypt.isMatch(nowPwd, user.getPayPwd())) throw new BaseException(MISS_MATCH_PWD);
        if (editPwd.length() != 6) throw new BaseException(MISS_MATCH_PWD_LENGTH);
        user.setPayPwd(bcrypt.encrypt(editPwd));
    }

    @Transactional
    public void findPayPwd(Long userId) {
        User user = userRepository.getById(userId);
        MailDto mailDto = mailService.createMail(user.getEmail());
        user.setPayPwd(bcrypt.encrypt(mailDto.getPwd()));
        mailService.sendMail(mailDto);
    }

    @Async
    @Transactional
    public void useMindalle(String prompt, Long userId, String targetToken, Long trafficId) throws IOException, InterruptedException, BaseException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://34.147.113.86:5000/mindalle"))
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(
                            "{\n\"prompt\": \"" + prompt + "\"\n}"))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                Long fcmId = fcmService.createFcm("그림 그리기에 실패하였습니다.", userId, null, null, null, null, null);
                fcmService.sendMessageTo(targetToken, fcmId, null, "그림 그리기에 실패하였습니다.", "그림을 그리는 중 오류가 발생하였습니다.");
                User user = userRepository.getById(userId);
                user.setMindalleCount(user.getMindalleCount() + 1);
                trafficService.finishTraffic(trafficId);
                throw new BaseException(FAILED_TO_GET_MINDALLE);
            }
            String res = response.body();
            if (res == null) {
                Long fcmId = fcmService.createFcm("그림 그리기에 실패하였습니다.", userId, null, null, null, null, null);
                fcmService.sendMessageTo(targetToken, fcmId, null, "그림 그리기에 실패하였습니다.", "그림을 그리는 중 오류가 발생하였습니다.");
                User user = userRepository.getById(userId);
                user.setMindalleCount(user.getMindalleCount() + 1);
                trafficService.finishTraffic(trafficId);
                throw new BaseException(FAILED_TO_GET_MINDALLE);
            }
            trafficService.finishTraffic(trafficId);
            String data[] = res.split("\"");
            Long fcmId = fcmService.createFcm("그림이 완성되었습니다", userId, data[3], data[7], data[11], data[15], null);
            List<String> urls = new ArrayList<>(Arrays.asList(data[3], data[7], data[11], data[15]));
            fcmService.sendMessageTo(targetToken, fcmId, urls, "그림이 완성되었습니다", "요청하신 그림이 완성되었습니다");
        } catch (ConnectException exception) {
            Long fcmId = fcmService.createFcm("그림 그리기에 실패하였습니다.", userId, null, null, null, null, null);
            fcmService.sendMessageTo(targetToken, fcmId, null, "그림 그리기에 실패하였습니다.", "그림을 그리는 중 오류가 발생하였습니다.");
            User user = userRepository.getById(userId);
            user.setMindalleCount(user.getMindalleCount() + 1);
            trafficService.finishTraffic(trafficId);
            String tt1 = userRepository.getById(1L).getDeviceToken();
            String tt2 = userRepository.getById(2L).getDeviceToken();
            String tt3 = userRepository.getById(3L).getDeviceToken();
            fcmService.sendMessageTo(tt1, fcmId, null, "❗서버에 문제가 발생하였습니다.", "민달리와의 연결이 끊어졌습니다.");
            fcmService.sendMessageTo(tt2, fcmId, null, "❗서버에 문제가 발생하였습니다.", "민달리와의 연결이 끊어졌습니다.");
            fcmService.sendMessageTo(tt3, fcmId, null, "❗서버에 문제가 발생하였습니다.", "민달리와의 연결이 끊어졌습니다.");
        }

    }

    public GetMyPageRes getMyPage(Long userId) throws IOException, ParseException, InterruptedException, BaseException {
        User user = userRepository.getById(userId);
        boolean isSetPwd = true;
        if (user.getPayPwd() == null || user.getPayPwd() == "" || user.getPayPwd().length() == 0) isSetPwd = false;
        return new GetMyPageRes(user.getUserId(), user.getNickname(), user.getAccountAddress(),
                user.getDescription(), user.getPhotoUrl(), utilService.getKlay(userId).getKlay(),
                coinService.getCoin(userId), user.getMindalleCount(), user.getMarketCount(), isSetPwd);
    }

    public List<GetCardListRes> getCards(Long userId, int pageId) {
        List<Card> cards = cardRepository.getCards(userId, PageRequest.of(pageId, 20)).orElse(null);
        List<GetCardListRes> cardResList = new ArrayList();
        for (int i = 0; i < cards.size(); i++) {
            cardResList.add(new GetCardListRes(cards.get(i).getCardId(), cards.get(i).getImgUrl()));
        }
        return cardResList;
    }

    public List<GetMarketListRes> getMarkets(Long userId, int pageId) {
        List<Market> markets = marketRepository.getMarkets(userId, PageRequest.of(pageId, 20)).orElse(null);
        List<GetMarketListRes> marketResList = new ArrayList();
        for (int i = 0; i < markets.size(); i++) {
            marketResList.add(new GetMarketListRes(markets.get(i).getMarketId(), markets.get(i).getCard().getImgUrl()));
        }
        return marketResList;
    }


    public GetUserPageRes getUserPage(Long userId) throws BaseException {
        if (!isValidUser(userId)) throw new BaseException(INVALID_USER_ID);
        User user = userRepository.getById(userId);
        return new GetUserPageRes(user.getUserId(), user.getNickname(), user.getAccountAddress(),
                user.getDescription(), user.getPhotoUrl());
    }

    public List<GetCardListRes> getUserCards(Long userId, Long anotherId, int pageId) throws BaseException {
        if (!isValidUser(anotherId)) throw new BaseException(INVALID_USER_ID);
        List<Card> cards = cardRepository.getCards(anotherId, PageRequest.of(pageId, 20)).orElse(null);
        List<GetCardListRes> cardResList = new ArrayList();
        if (userId == null) {
            for (int i = 0; i < cards.size(); i++) {
                cardResList.add(new GetCardListRes(cards.get(i).getCardId(), cards.get(i).getImgUrl()));
            }
        } else {
            User user = userRepository.getById(userId);
            for (int i = 0; i < cards.size(); i++) {
                if (user.getHideCards().contains(cards.get(i)))
                    continue;
                cardResList.add(new GetCardListRes(cards.get(i).getCardId(), cards.get(i).getImgUrl()));
            }
        }
        return cardResList;
    }

    public List<GetMarketListRes> getUserMarkets(Long userId, Long anotherId, int pageId) throws BaseException {
        if (!isValidUser(anotherId)) throw new BaseException(INVALID_USER_ID);

        List<Market> markets = marketRepository.getMarkets(anotherId, PageRequest.of(pageId, 20)).orElse(null);
        List<GetMarketListRes> marketResList = new ArrayList();
        if (userId == null) {
            for (int i = 0; i < markets.size(); i++) {
                marketResList.add(new GetMarketListRes(markets.get(i).getMarketId(), markets.get(i).getCard().getImgUrl()));
            }
        } else {
            User user = userRepository.getById(userId);
            for (int i = 0; i < markets.size(); i++) {
                if (user.getHideCards().contains(markets.get(i).getCard()))
                    continue;
                marketResList.add(new GetMarketListRes(markets.get(i).getMarketId(), markets.get(i).getCard().getImgUrl()));
            }
        }
        return marketResList;
    }

}
