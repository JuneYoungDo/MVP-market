package pathfinder.prodo.prodoserver.user;

import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pathfinder.prodo.prodoserver.appManage.Dto.GetVersionDto;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.config.BaseResponse;
import pathfinder.prodo.prodoserver.config.BaseResponseStatus;
import pathfinder.prodo.prodoserver.fcm.FcmService;
import pathfinder.prodo.prodoserver.market.MarketService;
import pathfinder.prodo.prodoserver.transaction.SalesHistory.SalesHistoryService;
import pathfinder.prodo.prodoserver.transaction.accountTx.AccountTXService;
import pathfinder.prodo.prodoserver.transaction.traffic.TrafficRes;
import pathfinder.prodo.prodoserver.user.Dto.*;
import pathfinder.prodo.prodoserver.utils.JwtService;
import pathfinder.prodo.prodoserver.utils.S3Service;
import pathfinder.prodo.prodoserver.utils.UtilService;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class UserController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;
    private final MarketService marketService;
    private final UserRepository userRepository;        // for test
    private final JwtService jwtService;
    private final S3Service s3Service;
    private final AccountTXService accountTXService;
    private final SalesHistoryService salesHistoryService;
    private final FcmService fcmService;
    private final UtilService utilService;

    @GetMapping("/test")
    public ResponseEntity test() {
        System.out.println(LocalDateTime.now());
        return new ResponseEntity(200, HttpStatus.valueOf(200));
    }

    /**
     * 앱 최신 버전 확인 api
     * [GET] /app/version
     */
    @GetMapping("/app/version")
    public ResponseEntity getAppVersion() {
        return new ResponseEntity(new GetVersionDto(userService.getAppVersion()), HttpStatus.valueOf(200));
    }

    /**
     * 민달리 이미지를 업로드 하는 api
     */
    @PostMapping("/upload/ai")
    public ResponseEntity chainAiServer(@RequestBody MultipartFile file) throws IOException {
        return new ResponseEntity(s3Service.uploadImg(file), HttpStatus.valueOf(200));
    }

    /**
     * 민달리 서버의 api를 이용하는 api
     */
    @PostMapping("/mindalle")
    public ResponseEntity useMindallE(@RequestBody UseMindalleReq useMindalleRes) throws IOException, InterruptedException {
        try {
            Long userId = jwtService.getUserId();
            TrafficRes trafficRes = userService.checkForMindalle(userId);
            userService.useMindalle(useMindalleRes.getPrompt(), userId, userRepository.getById(userId).getDeviceToken(), trafficRes.getTrafficId());
            return new ResponseEntity(new MindalleRes(trafficRes.getWaitNum()), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_ON_SERVER), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_ON_SERVER.getStatus()));
        }
    }

    /**
     * 카카오 로그인 api
     */
    @PostMapping("/kakao/login")
    public ResponseEntity kakaoLogin(@RequestBody KakaoLoginReq kakaoLoginReq) {
        try {
            return new ResponseEntity(userService.kakaoLogin(kakaoLoginReq.getIdToken(), kakaoLoginReq.getDeviceToken()),
                    HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | ParseException | InterruptedException e) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_CREATE_ACCOUNT), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_CREATE_ACCOUNT.getStatus()));
        }
    }

    /**
     * refreshToken을 이용한 accessToken 재발급
     * [GET] /login/refresh
     */
    @GetMapping("/login/refresh")
    public ResponseEntity renewalAccessToken() {
        try {
            String refreshToken = jwtService.getRefreshJwt();
            Long userId = jwtService.getUserIdFromRefreshToken();
            return new ResponseEntity(userService.renewalAccessToken(userId, refreshToken), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 마이페이지 확인하기
     * [GET] /user
     */
    @GetMapping("/user")
    public ResponseEntity getMyPage() {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(userService.getMyPage(userId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_GET_KLAY), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_GET_KLAY.getStatus()));
        }
    }

    /**
     * 닉네임 중복 확인하기
     * [POST] /nickname/check
     */
    @PostMapping("/nickname/check")
    public ResponseEntity checkNickname(@RequestBody CheckNicknameReq checkNicknameReq) {
        return new ResponseEntity(userService.checkNickname(checkNicknameReq.getNickname()), HttpStatus.valueOf(200));
    }

    /**
     * 디바이스 토큰 수정하기
     * [PUT] /deviceToken
     */
    @PutMapping("/deviceToken")
    public ResponseEntity editDeviceToken(@RequestBody EditDeviceTokenReq editDeviceTokenReq) {
        try {
            Long userId = jwtService.getUserId();
            userService.editDeviceToken(userId, editDeviceTokenReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 개인정보 수정하기
     * [PUT] /user
     */
    @PutMapping("/user")
    public ResponseEntity editUserPage(@RequestBody EditUserPageReq editUserPageReq) {
        try {
            Long userId = jwtService.getUserId();
            userService.editUserPage(userId, editUserPageReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 결제 비밀번호 설정하기
     * [POST] /user/pwd
     */
    @PostMapping("/user/pwd")
    public ResponseEntity setPayPwd(@RequestBody SetPwdReq setPwdReq) {
        try {
            Long userId = jwtService.getUserId();
            userService.setPayPwd(userId, setPwdReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 결제 비밀번호 수정하기
     * [PUT] /user/pwd
     */
    @PutMapping("/user/pwd")
    public ResponseEntity editPayPwd(@RequestBody EditPwdReq editPwdReq) {
        try {
            Long userId = jwtService.getUserId();
            userService.editPayPwd(userId, editPwdReq);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 결제 비밀번호 찾기
     * [GET] /user/pwd
     */
    @GetMapping("/user/pwd")
    public ResponseEntity findPayPwd() {
        try {
            Long userId = jwtService.getUserId();
            userService.findPayPwd(userId);
            return new ResponseEntity(200, HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


    /**
     * 작품 판매, 구매 내역 확인하기
     * [GET] /user/sale/history/{pageId}
     */
    @GetMapping("/user/sale/history/{pageId}")
    public ResponseEntity getSalesHistory(@PathVariable int pageId) {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(salesHistoryService.getSalesHistory(userId, pageId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 푸쉬 알림 목록 확인하기
     * [GET] /user/messages/{pageId}
     */
    @GetMapping("/user/messages/{pageId}")
    public ResponseEntity getMessageHistory(@PathVariable int pageId) {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(fcmService.getFcmHistory(userId, pageId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


    /**
     * user account TX 확인하기
     * [GET] /user/tx/{pageId}
     */
    @GetMapping("/user/tx/{pageId}")
    public ResponseEntity getAccountTx(@PathVariable int pageId) {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(accountTXService.getAccountTx(userId, pageId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 마이페이지 내 작품 피드 가져오기
     * [GET] /user/cards/{pageID}
     */
    @GetMapping("/user/cards/{pageId}")
    public ResponseEntity getMyCardList(@PathVariable int pageId) {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(userService.getCards(userId, pageId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 마이페이지 내 마켓 피드 가져오기
     * [GET] /user/markets/{pageId}
     */
    @GetMapping("/user/markets/{pageId}")
    public ResponseEntity getMyMarketList(@PathVariable int pageId) {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(userService.getMarkets(userId, pageId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 내 클레이 확인하기
     * [GET] /user/klay
     */
    @GetMapping("/user/klay")
    public ResponseEntity getKlay() {
        try {
            Long userId = jwtService.getUserId();
            return new ResponseEntity(utilService.getKlay(userId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        } catch (IOException | InterruptedException | ParseException exception) {
            return new ResponseEntity(new BaseResponse(BaseResponseStatus.FAILED_TO_GET_KLAY), HttpStatus.valueOf(
                    BaseResponseStatus.FAILED_TO_GET_KLAY.getStatus()));
        }
    }


    /**
     * 다른 사용자 정보 확인하기
     * [GET] /user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity getUserPage(@PathVariable Long userId) {
        try {
            return new ResponseEntity(userService.getUserPage(userId), HttpStatus.valueOf(200));
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


    /**
     * 다른 사용자 작품 피드 가져오기
     * [GET] /user/{userId}/cards/{pageId}
     */
    @GetMapping("/user/{userId}/cards/{pageId}")
    public ResponseEntity getUserCardList(@PathVariable Long userId, @PathVariable int pageId) {
        try {
            if (jwtService.getJwt() == null || jwtService.getJwt().equals("") || jwtService.getJwt().length() == 0)
                return new ResponseEntity(userService.getUserCards(null, userId, pageId), HttpStatus.valueOf(200));
            else {
                Long myId = jwtService.getUserId();
                return new ResponseEntity(userService.getUserCards(myId, userId, pageId), HttpStatus.valueOf(200));
            }
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }

    /**
     * 다른 사용자 마켓 피드 가져오기
     * [GET] /user/{userId}/markets/{pageId}
     */
    @GetMapping("/user/{userId}/markets/{pageId}")
    public ResponseEntity getUserMarketList(@PathVariable Long userId, @PathVariable int pageId) {
        try {
            if (jwtService.getJwt() == null || jwtService.getJwt().equals("") || jwtService.getJwt().length() == 0)
                return new ResponseEntity(userService.getUserMarkets(null, userId, pageId), HttpStatus.valueOf(200));
            else {
                Long myId = jwtService.getUserId();
                return new ResponseEntity(userService.getUserMarkets(myId, userId, pageId), HttpStatus.valueOf(200));
            }
        } catch (BaseException exception) {
            return new ResponseEntity(new BaseResponse(exception.getStatus()), HttpStatus.valueOf(exception.getStatus().getStatus()));
        }
    }


}
