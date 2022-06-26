package pathfinder.prodo.prodoserver.market.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadMarketReq {
    private String payPwd;
    private Long cardId;
    private double pay;
}
