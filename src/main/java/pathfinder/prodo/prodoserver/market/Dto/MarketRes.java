package pathfinder.prodo.prodoserver.market.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pathfinder.prodo.prodoserver.transaction.cardTx.dto.GetCardTxRes;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MarketRes {
    private Long marketId;
    private String contractsAddress;
    private Long cardId;
    private String id;
    private String imgUrl;
    private String title;
    private String description;
    private String txHash;
    private String pay;
    private Long ownerId;
    private String owner;
    private String ownerImgUrl;
    private LocalDateTime createdAt;
    private boolean isMyCard;
    private String type;
    private List<GetCardTxRes> cardTx;
}
