package pathfinder.prodo.prodoserver.fcm;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "fcm")
public class Fcm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmId;
    private String type;
    private Long userId;
    private String url1;
    private String url2;
    private String url3;
    private String url4;
    private Long cardId;
    private boolean watched;
    private LocalDateTime updatedAt;
}
