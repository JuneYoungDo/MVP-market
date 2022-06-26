package pathfinder.prodo.prodoserver.transaction.coinTx;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CoinTxService {
    private final CoinTxRepository coinTxRepository;

    @Transactional
    public void save(CoinTx coinTx) {
        coinTxRepository.save(coinTx);
    }

    public void createCoinTx(Long userId, double amount, String type) {
        CoinTx coinTx = CoinTx.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .createdAt(LocalDateTime.now())
                .build();
        save(coinTx);
    }
}
