package pathfinder.prodo.prodoserver.card.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardHideListRes {
    private Long cardId;
    private String imgUrl;
    private String title;
    private String owner;
}
