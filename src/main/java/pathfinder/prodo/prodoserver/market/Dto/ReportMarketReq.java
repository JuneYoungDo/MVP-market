package pathfinder.prodo.prodoserver.market.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ReportMarketReq {
    private Long marketId;
    private String description;
}
