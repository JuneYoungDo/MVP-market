package pathfinder.prodo.prodoserver.coin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ExchangeDalleToLpReq {
    private String payPwd;
    private double amount;
}
