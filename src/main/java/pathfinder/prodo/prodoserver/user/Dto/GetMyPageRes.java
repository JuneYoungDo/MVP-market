package pathfinder.prodo.prodoserver.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetMyPageRes {
    private Long userId;
    private String nickname;
    private String accountAddress;
    private String description;
    private String photoUrl;
    private String klay;
    private String dali;
    private Long mindalleCount;
    private Long marketCount;
    private boolean isSetPwd;
}
