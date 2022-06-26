package pathfinder.prodo.prodoserver.card.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetCardIssueRes {
    private Long cardId;
    private String title;
    private String imgUrl;
    private LocalDateTime createdAt;
}
