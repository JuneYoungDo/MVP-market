package pathfinder.prodo.prodoserver.market.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MainFeedRes {
    private Long marketId;
    private String imgUrl;
    private String title;
    private String owner;
    private String ownerImgUrl;
    private String pay;
}
