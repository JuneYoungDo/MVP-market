package pathfinder.prodo.prodoserver.transaction.cardIssueHistory;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "card_issue_TX")
public class CardIssueTx {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long issueTxId;
    private Long cardId;
    private Long ownerId;
    private LocalDateTime updatedAt;
}
