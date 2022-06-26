package pathfinder.prodo.prodoserver.transaction.liquidityPool;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "liquidity")
public class Liquidity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long liquidityId;
    private Long userId;
    private String type;
    private double lp;
    private double klay;
    private double dalle;
    private LocalDateTime createdAt;
}
