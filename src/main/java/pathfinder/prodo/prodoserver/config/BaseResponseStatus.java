package pathfinder.prodo.prodoserver.config;

import lombok.Getter;

import java.sql.Timestamp;

@Getter
public enum BaseResponseStatus {
    EMPTY_REFRESH_TOKEN(400,"refresh token 이 없습니다.",100),
    EMPTY_JWT(400, "Jwt가 없습니다.", 101),
    INVALID_JWT(401,"유효하지 않은 Jwt 입니다.",102),
    INVALID_TOKEN(400,"유효하지 않은 토큰입니다.",103),
    ALREADY_IN_MARKET(400,"이미 마켓에 올라가있는 카드입니다.",104),
    INVALID_USER_ID(400,"유효하지 않은 유저 id 입니다.",105),
    INVALID_NFT_ID(400,"유효하지 않은 NFT id 입니다.",106),
    NOT_ENOUGH_KLAY(400,"KLAY 가 충분하지 않습니다.",107),
    NOT_EXIST_MARKET(400,"존재하지 않는 market id 입니다.",108),
    INVALID_CARD_ID(400,"유효하지 않은 카드 id 입니다.",109),
    IS_NOT_YOUR_CARD(400,"본인의 카드가 아닙니다.",110),
    IS_YOUR_CARD(400,"본인의 카드 입니다.",111),
    INVALID_ID_TOKEN(400,"유효하지 않은 id 토큰입니다.",112),
    CAN_NOT_BLANK_NICKNAME(400,"닉네임은 공백일 수 없습니다.",113),
    IS_USED_NICKNAME(400,"이미 사용중인 닉네임 입니다.",114),
    INVALID_MARKET_ID(400,"유효하지 않은 마켓 id 입니다.",115),
    CAN_NOT_BLANK_DEVICE_TOKEN(400,"디바이스 토큰은 공백일 수 없습니다.",116),
    ITS_YOUR_MARKET(400,"본인의 마켓입니다.",117),
    ALREADY_BLOCKED_CARD(400,"이미 차단된 카드입니다.",117),
    ALREADY_CLEAR_CARD(400,"이미 차단이 해제된 카드입니다.",118),
    NOT_ENOUGH_LENGTH(400,"신고사유를 적어주세요",119),
    PLEASE_WAIT_FOR_COMMIT(400,"아직 최근 트랜잭션이 커밋되지 않았습니다.",120),
    CAN_NOT_SEND_MYSELF(400,"본인이 본인에게 전송할 수 없습니다.",121),
    TOO_MUCH_TRAFFIC(400,"현재 트래픽 과부하로 잠시 후 이용해 주세요.",122),
    ALREADY_SET_PWD(400,"이미 설정된 비밀번호가 있습니다.",123),
    MISS_MATCH_PWD_LENGTH(400,"결제 비밀번호는 6자리여야 합니다.",124),
    MISS_MATCH_PWD(400,"현재 비밀번호가 일치하지 않습니다.",125),
    EMPTY_PAY_PWD(400,"결제 비밀번호가 없습니다. 먼저 결제 비밀번호를 등록하여 주세요.",126),
    IS_NOT_YOUR_MARKET(400,"본인의 마켓이 아닙니다.",127),
    NOT_ENOUGH_DALI(400,"dalle 가 충분하지 않습니다.",128),
    INVALID_ACCOUNT_FOR_DALLE(400,"dalle를 사용할 수 있는 계좌가 아닙니다.",129),
    NOT_ENOUGH_MANAGE_KLAY(400,"관리 되고 있는 클레이가 부족합니다.",130),
    NOT_ENOUGH_LP(400,"LP 코인이 충분하지 않습니다.",131),
    FAILED_SWAP_NOT_ENOUGH_DALLE(400,"유동성 풀에 달리가 부족합니다.",132),
    FAILED_SWAP_NOT_ENOUGH_KLAY(400,"유동성 풀에 클레이가 부족합니다.",133),

    FAILED_TO_SEND_COIN(500,"코인 전송에 실패하였습니다.",188),
    FAILED_TO_MAKE_COIN(500,"코인 발급에 실패하였습니다.",189),
    FAILED_TO_GET_BITHUMB(500,"빗썸의 거래가 막혀있습니다.",190),
    FAILED_TO_GET_MARKET_PRICE(500,"클레이 가격 확인에 실패하였습니다.",191),
    FAILED_TO_GET_COIN(500,"코인 확인에 실패하였습니다.",192),
    FAILED_TO_GET_MINDALLE(500,"그림 그리기에 실패하였습니다.",193),
    FAILED_TO_CREATE_ACCOUNT(500,"계좌 개설에 실패하였습니다.",194),
    FAILED_TO_BUY_NFT(500,"NFT 구매에 실패하였습니다.",195),
    FAILED_TO_SEND_NFT(500,"NFT 전송에 실패하였습니다.",196),
    FAILED_TO_SEND_KLAY(500,"KLAY 전송에 실패하였습니다.",197),
    FAILED_TO_GET_KLAY(500,"KLAY 확인에 실패하였습니다.",198),
    FAILED_ON_SERVER(500,"예기치 못한 에러가 발생하였습니다.",199),
    FAILED_TO_SWAP(500,"스왑에 실패하였습니다.",200),
    MAXIMUM_DALI(400,"dalle 코인의 한도가 초과되었습니다.",201),
    MAXIMUM_LP(400,"Lp 코인의 한도가 초과되었습니다.",202),
    ;

    private final Timestamp timestamp;
    private final int status;
    private String message;
    private final int code;

    BaseResponseStatus(int status, String message, int code) {
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
