package pathfinder.prodo.prodoserver.transaction.accountTx;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "accountTX")
public class AccountTX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountTxId;
    private String type;
    private double pay;
    private double pay2;
    private double lp;
    private Long senderId;
    private Long receiverId;
    private String receiverAccountAddress;
    private LocalDateTime updatedAt;
}
