package pathfinder.prodo.prodoserver.card.VO;

import lombok.*;
import pathfinder.prodo.prodoserver.market.VO.Market;
import pathfinder.prodo.prodoserver.user.VO.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;
    private String type;
    private String id;
    private String title;
    private String description;
    private String TxHash;
    private String imgUrl;
    private boolean deleted;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "createdBy")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "card", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Market> markets;
}
