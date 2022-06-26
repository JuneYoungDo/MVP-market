package pathfinder.prodo.prodoserver.utils.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MailDto {
    private String address;
    private String title;
    private String message;
    private String pwd;
}
