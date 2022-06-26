package pathfinder.prodo.prodoserver.transaction.traffic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class TrafficService {

    private final TrafficRepository trafficRepository;

    @Transactional
    public Long createAiTraffic() {
        AiTraffic aiTraffic = AiTraffic.builder()
                .finished(false)
                .build();
        trafficRepository.save(aiTraffic);
        return aiTraffic.getTrafficId();
    }

    public Long getCountTraffic() {
        return trafficRepository.countTraffic().orElse(0L);
    }

    @Transactional
    public void finishTraffic(Long trafficId) {
        AiTraffic aiTraffic = trafficRepository.getById(trafficId);
        aiTraffic.setFinished(true);
    }
}
