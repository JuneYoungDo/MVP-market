package pathfinder.prodo.prodoserver.coin.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SendDalleReq {
    public String payPwd;
    public String receiverAccountAddress;
    public double dalle;
}
