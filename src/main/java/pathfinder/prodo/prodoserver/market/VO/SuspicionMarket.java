package pathfinder.prodo.prodoserver.market.VO;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Builder
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Entity(name = "suspicion_market")
public class SuspicionMarket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long suspicionId;
    private Long reportUserId;
    private Long suspicionMarketId;
    private String description;
}
