package pathfinder.prodo.prodoserver.transaction.cardTx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetCardTxRes {
    private Long userId;
    private String imgUrl;
    private String nickname;
    private String accountAddress;
    private LocalDateTime updatedAt;
}
