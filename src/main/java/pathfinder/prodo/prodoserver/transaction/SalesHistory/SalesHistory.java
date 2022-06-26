package pathfinder.prodo.prodoserver.transaction.SalesHistory;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sales_history")
public class SalesHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long salesId;
    private Long sellerId;
    private Long buyerId;
    private Long cardId;
    private double pay;
    private LocalDateTime updatedAt;
}
