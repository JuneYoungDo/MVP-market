package pathfinder.prodo.prodoserver.transaction.traffic;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ai_traffic")
public class AiTraffic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trafficId;
    private boolean finished;
}
