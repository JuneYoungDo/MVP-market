package pathfinder.prodo.prodoserver.utils;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.config.BaseException;
import pathfinder.prodo.prodoserver.transaction.transaction.Transaction;
import pathfinder.prodo.prodoserver.transaction.transaction.TransactionRepository;
import pathfinder.prodo.prodoserver.user.Dto.GetKlayRes;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.List;

import static pathfinder.prodo.prodoserver.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class UtilService {
    private final Bcrypt bcrypt;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public String doubleToString(double d) {
        return String.format("%.6f", Double.valueOf(d * 1000000).longValue() / 1000000f);
    }

    public JSONObject parseBody(HttpResponse<String> response) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(response.body());
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject;
    }

    public String hexToPeb(String hex) {
        DecimalFormat format = new DecimalFormat();
        format.applyLocalizedPattern("0.000000");
        String hexKlay = hex.substring(2);      // 앞 2글자에서 코드 자르기 ex) 0x0 -> 0
        BigInteger bigInteger = new BigInteger(hexKlay, 16);     // 16진수 -> 10진수 bigInteger
        BigInteger naun = new BigInteger("1000000000000");      // 10의 12제곱 bigInteger
        Long re = bigInteger.divide(naun).longValue();              // 클레이 나누기 10의 12제곱
        return format.format(Math.floor((double) re)/1000000.0);
    }

    public String decimalToHex(double d) {
        Long pay = Double.valueOf(d * 1000000).longValue();         // 소수점 6째까지 받아서 곱해서 넘김
        String py = pay.toString();
        BigInteger bigInteger = new BigInteger(py);                  // 보내려는 클레이 정수의 BigInteger
        BigInteger gob = new BigInteger("1000000000000");      // 10의 10제곱 BigInteger
        String hex = bigInteger.multiply(gob).toString(16);     // 정수 곱하기 10의 16제곱 을 16진수로 표현
        String res = "0x" + hex;
        return res;
    }

    public boolean checkPwd(User user, String payPwd) throws BaseException {
        if (user.getPayPwd() == null || user.getPayPwd().equals("") || user.getPayPwd().length() == 0)
            throw new BaseException(EMPTY_PAY_PWD);
        if (!bcrypt.isMatch(payPwd, user.getPayPwd()))
            throw new BaseException(MISS_MATCH_PWD);
        return true;
    }

    public boolean isCommittedTx(String txHash) throws IOException, InterruptedException, BaseException, ParseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wallet-api.klaytnapi.com/v2/tx/" + txHash))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_ON_SERVER);
        JSONObject jsonObject = parseBody(response);
        String status = (String) jsonObject.get("status");
        if (status.equals("Committed")) return true;
        else return false;
    }

    public boolean checkTransactionCommit(Long userId) throws BaseException, IOException, ParseException, InterruptedException {
        List<Transaction> recentTxs = transactionRepository.getRecentlyTx(userId, PageRequest.of(0, 1)).orElse(null);
        if (recentTxs.size() != 0)
            if (!isCommittedTx(recentTxs.get(0).getTxHash()))
                throw new BaseException(PLEASE_WAIT_FOR_COMMIT);
        return true;
    }

    public GetKlayRes getKlay(Long userId) throws IOException, InterruptedException, ParseException, BaseException {
        User user = userRepository.getById(userId);
        String userAccount = user.getAccountAddress();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://node-api.klaytnapi.com/v1/klaytn"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString(
                        "{\n  \"id\": 1,\n  \"jsonrpc\": \"2.0\",\n  \"method\": \"klay_getBalance\",\n  \"params\": [ \"" + userAccount + "\",\"latest\"]\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_KLAY);
        JSONObject jsonObject = parseBody(response);
        String tmp = (String) jsonObject.get("result");
        if (tmp == null) throw new BaseException(FAILED_TO_GET_KLAY);
        return new GetKlayRes(hexToPeb(tmp));
    }

    public double getKlayFromAddress(String address) throws IOException, InterruptedException, BaseException, ParseException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://node-api.klaytnapi.com/v1/klaytn"))
                .header("Content-Type", "application/json")
                .header("x-chain-id", Secret.KAS_SERVER_VERSION)
                .header("Authorization", Secret.KAS_AUTHORIZATION)
                .method("POST", HttpRequest.BodyPublishers.ofString(
                        "{\n  \"id\": 1,\n  \"jsonrpc\": \"2.0\",\n  \"method\": \"klay_getBalance\",\n  \"params\": [ \"" + address + "\",\"latest\"]\n}"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new BaseException(FAILED_TO_GET_KLAY);
        JSONObject jsonObject = parseBody(response);
        String tmp = (String) jsonObject.get("result");
        if (tmp == null) throw new BaseException(FAILED_TO_GET_KLAY);
        return Double.parseDouble(hexToPeb(tmp));
    }

}
