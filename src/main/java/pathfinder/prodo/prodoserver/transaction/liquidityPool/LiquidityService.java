package pathfinder.prodo.prodoserver.transaction.liquidityPool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.transaction.liquidityPool.dto.GetLpInfo;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidityService {

    private final LiquidityRepository liquidityRepository;

    @Transactional
    public void save(Liquidity liquidity) {
        liquidityRepository.save(liquidity);
    }

    public void create(Long userId, String type, double lp, double klay, double dalle) {
        Liquidity liquidity = Liquidity.builder()
                .userId(userId)
                .type(type)
                .lp(lp)
                .klay(klay)
                .dalle(dalle)
                .createdAt(LocalDateTime.now())
                .build();
        save(liquidity);
    }

    public double getAllLp() {
        double allLp = 0;
        List<Liquidity> allLiquidity = liquidityRepository.getAllLiquidity().orElse(null);
        if (allLiquidity != null) {
            for (int i = 0; i < allLiquidity.size(); i++) {
                Liquidity liquidity = allLiquidity.get(i);
                if (liquidity.getType().equals("In")) {
                    allLp = allLp + liquidity.getLp();
                } else {
                    allLp = allLp - liquidity.getLp();
                }
            }
        }
        return allLp;
    }

    public GetLpInfo getLpInfoFromUserId(Long userId) {
        double klay = 0;
        double dalle = 0;
        List<Liquidity> liquidities = liquidityRepository.getLiquidity(userId).orElse(null);
        if (liquidities != null) {
            for (int i = 0; i < liquidities.size(); i++) {
                Liquidity liquidity = liquidities.get(i);
                if (liquidity.getType().equals("In")) {
                    klay = klay + liquidity.getKlay();
                    dalle = dalle + liquidity.getDalle();
                } else {
                    klay = klay - liquidity.getKlay();
                    dalle = dalle - liquidity.getDalle();
                }
            }
        }
        return new GetLpInfo(klay, dalle);
    }
}
