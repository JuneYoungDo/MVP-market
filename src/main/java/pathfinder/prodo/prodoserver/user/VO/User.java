package pathfinder.prodo.prodoserver.user.VO;

import lombok.*;
import pathfinder.prodo.prodoserver.card.VO.Card;
import pathfinder.prodo.prodoserver.market.VO.Market;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String email;
    private String accountAddress;
    private String nickname;
    private String description;
    private String photoUrl;
    private String refreshToken;
    private String deviceToken;
    private Long mindalleCount;
    private Long marketCount;
    private String payPwd;
    private boolean deleted;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Card> cards;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Market> markets;

    @ManyToMany
    @JoinTable(name = "card_hide_list",
            joinColumns = @JoinColumn(name = "user_id"))
    private List<Card> hideCards;
}
