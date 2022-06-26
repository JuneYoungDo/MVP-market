package pathfinder.prodo.prodoserver.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.card.CardRepository;
import pathfinder.prodo.prodoserver.user.Dto.GetFcmHistoryRes;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmService {
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/pathfinder-101/messages:send";
    private final ObjectMapper objectMapper;
    private final FcmRepository fcmRepository;
    private final CardRepository cardRepository;

    @Transactional
    public void save(Fcm fcm) {
        fcmRepository.save(fcm);
    }

    public Long createFcm(String type, Long userId, String url1, String url2, String url3, String url4, Long cardId) {
        Fcm fcm = Fcm.builder()
                .type(type)
                .userId(userId)
                .url1(url1).url2(url2).url3(url3).url4(url4)
                .cardId(cardId)
                .watched(false)
                .updatedAt(LocalDateTime.now())
                .build();
        save(fcm);
        return fcm.getFcmId();
    }

    public void sendMessageTo(String targetToken, Long fcmId, List<String> urls, String title, String body) throws IOException {
        String message;
        if (urls == null) message = makeMessage(targetToken, fcmId, null, title, body);
        else message = makeMessage(targetToken, fcmId, urls, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request)
                .execute();
        System.out.println(response);
    }

    private String makeMessage(String targetToken, Long fcmId, List<String> urls, String title, String body) throws JsonProcessingException {
        HashMap map = new HashMap();
        if (urls != null) {
            map.put("url1", urls.get(0));
            map.put("url2", urls.get(1));
            map.put("url3", urls.get(2));
            map.put("url4", urls.get(3));
        }
        map.put("fcmId", Long.toString(fcmId));
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .build()
                        )
                        .data(map)
                        .build()
                )
                .validate_only(false)
                .build();
        return objectMapper.writeValueAsString(fcmMessage);
    }


    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/pathfinder-101-firebase-adminsdk-atmib-d45db3611a.json";
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public List<GetFcmHistoryRes> getFcmHistory(Long userId, int pageId) {
        List<Fcm> fcmList = fcmRepository.getFcmHistory(userId, PageRequest.of(pageId, 20)).orElse(null);
        List<GetFcmHistoryRes> fcmHistoryResList = new ArrayList<>();
        for (int i = 0; i < fcmList.size(); i++) {
            Fcm fcm = fcmList.get(i);
            String title;
            String imgUrl;
            if (fcm.getCardId() == null) {
                title = null;
                imgUrl = null;
            } else {
                title = cardRepository.getById(fcm.getCardId()).getTitle();
                imgUrl = cardRepository.getById(fcm.getCardId()).getImgUrl();
            }
            fcmHistoryResList.add(
                    new GetFcmHistoryRes(
                            fcm.getFcmId(),
                            fcm.getType(),
                            fcm.getUrl1(),
                            fcm.getUrl2(),
                            fcm.getUrl3(),
                            fcm.getUrl4(),
                            fcm.isWatched(),
                            fcm.getCardId(),
                            imgUrl,
                            title,
                            fcm.getUpdatedAt()
                    )
            );
        }
        return fcmHistoryResList;
    }
}
