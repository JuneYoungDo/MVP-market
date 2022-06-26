package pathfinder.prodo.prodoserver.card.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pathfinder.prodo.prodoserver.transaction.cardTx.dto.GetCardTxRes;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CardRes {
    private Long cardId;
    private String contractsAddress;
    private String id;
    private String imgUrl;
    private String title;
    private String description;
    private String txHash;
    private LocalDateTime createdAt;
    private boolean isUploaded;
    private boolean isMine;
    private String type;
    private List<GetCardTxRes> cardTx;
}
