package pathfinder.prodo.prodoserver.card.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveCardReq {
    private String type;
    private String id;
    private String title;
    private String description;
    private String imgUrl;
    private String txHash;
    private Long fcmId;
}
