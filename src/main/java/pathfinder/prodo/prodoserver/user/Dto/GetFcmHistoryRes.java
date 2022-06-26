package pathfinder.prodo.prodoserver.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetFcmHistoryRes {
    private Long fcmId;
    private String type;

    private String url1;
    private String url2;
    private String url3;
    private String url4;
    private boolean watched;

    private Long cardId;
    private String imgUrl;
    private String title;

    private LocalDateTime createdAt;
}
