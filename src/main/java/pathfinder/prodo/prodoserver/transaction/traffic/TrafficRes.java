package pathfinder.prodo.prodoserver.transaction.traffic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class TrafficRes {
    private Long trafficId;
    private Long waitNum;
}
