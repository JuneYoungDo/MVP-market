package pathfinder.prodo.prodoserver.coin.VO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "coin_count")
public class CoinCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long countId;
    private String daliCount;
}
