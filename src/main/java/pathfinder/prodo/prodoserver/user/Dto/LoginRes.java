package pathfinder.prodo.prodoserver.user.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class LoginRes {
    private String accessToken;
    private String refreshToken;
}
