package pathfinder.prodo.prodoserver.transaction.transaction;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long txId;
    private Long senderId;
    private Long receiverId;
    private String txHash;
    private LocalDateTime createdAt;
}
