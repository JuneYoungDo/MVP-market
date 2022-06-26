package pathfinder.prodo.prodoserver.transaction.coinTx;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "coin_tx")
public class CoinTx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coinTxId;
    private Long userId;
    private double amount;
    private String type;
    private LocalDateTime createdAt;
}
