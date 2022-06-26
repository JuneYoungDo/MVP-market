package pathfinder.prodo.prodoserver.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GetUserPageRes {
    private Long userId;
    private String nickname;
    private String accountAddress;
    private String description;
    private String photoUrl;
}
