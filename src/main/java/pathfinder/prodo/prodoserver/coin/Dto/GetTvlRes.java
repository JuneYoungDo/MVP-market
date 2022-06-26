package pathfinder.prodo.prodoserver.coin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetTvlRes {
    private String TVL;
    private String dallE;
    private String klay;
    private String allLp;
}
