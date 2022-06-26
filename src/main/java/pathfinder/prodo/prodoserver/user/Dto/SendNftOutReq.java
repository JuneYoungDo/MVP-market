package pathfinder.prodo.prodoserver.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SendNftOutReq {
    private String payPwd;
    private String receiverAccount;
    private Long cardId;
}
