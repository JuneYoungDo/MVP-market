package pathfinder.prodo.prodoserver.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetSalesHistoryRes {
    private String sales;
    private Long userId;
    private String nickname;
    private Long cardId;
    private String imgUrl;
    private String title;
    private String pay;
    private LocalDateTime updatedAt;
}
