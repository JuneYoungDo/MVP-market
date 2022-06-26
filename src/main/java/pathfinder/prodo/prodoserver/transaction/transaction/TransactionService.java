package pathfinder.prodo.prodoserver.transaction.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Transactional
    public void save(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void createTx(Long senderId, Long receiverId, String txHash) {
        Transaction transaction = Transaction.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .txHash(txHash)
                .createdAt(LocalDateTime.now())
                .build();
        save(transaction);
    }
}
