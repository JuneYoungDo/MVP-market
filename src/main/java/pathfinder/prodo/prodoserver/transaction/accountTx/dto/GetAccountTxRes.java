package pathfinder.prodo.prodoserver.transaction.accountTx.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetAccountTxRes {
    private String type;
    private String pay1;                        // 보낸거
    private String pay2;                        // 받은거
    private String lp;
    private String senderNickname;
    private String senderAccountAddress;
    private String receiverNickname;
    private String receiverAccountAddress;
    private LocalDateTime updatedAt;
}
