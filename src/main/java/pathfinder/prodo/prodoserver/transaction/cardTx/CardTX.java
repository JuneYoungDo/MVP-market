package pathfinder.prodo.prodoserver.transaction.cardTx;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cardTX")
public class CardTX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardTxId;
    private Long cardId;
    private Long ownerId;
    private LocalDateTime updatedAt;
}
