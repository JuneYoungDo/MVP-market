package pathfinder.prodo.prodoserver.transaction.accountTx;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.transaction.accountTx.dto.GetAccountTxRes;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.UtilService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountTXService {
    private final AccountTXRepository accountTXRepository;
    private final UserRepository userRepository;
    private final UtilService utilService;

    @Transactional
    public void save(AccountTX accountTX) {
        accountTXRepository.save(accountTX);
    }

    public void createdAccountTx(String type, double pay1, double pay2, double lp, Long senderId, Long receiverId, String receiverAccountAddress) {
        AccountTX accountTX;
        if (receiverAccountAddress == null) {       // 내부 계좌
            accountTX = AccountTX.builder()
                    .type(type)
                    .pay(pay1)
                    .pay2(pay2)
                    .lp(lp)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .receiverAccountAddress("")
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {            // 외부 계좌
            accountTX = AccountTX.builder()
                    .type(type)
                    .pay(pay1)
                    .pay2(pay2)
                    .lp(lp)
                    .senderId(senderId)
                    .receiverId(null)
                    .receiverAccountAddress(receiverAccountAddress)
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
        save(accountTX);
    }

    public List<GetAccountTxRes> getAccountTx(Long userId, int pageId) {
        List<AccountTX> accountTXList = accountTXRepository.getAccountTX(userId, PageRequest.of(pageId, 20)).orElse(null);
        List<GetAccountTxRes> getAccountTxRes = new ArrayList<>();
        for (int i = 0; i < accountTXList.size(); i++) {
            AccountTX accountTX = accountTXList.get(i);
            if (accountTX.getType() == null) {
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        accountTX.getUpdatedAt()
                ));
            } else if (accountTX.getType().equals("swap1") || accountTX.getType().equals("swap2")) {
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),
                        utilService.doubleToString(accountTX.getPay2()),
                        "",
                        userRepository.getById(accountTX.getSenderId()).getNickname(),
                        userRepository.getById(accountTX.getSenderId()).getAccountAddress(),
                        "",
                        "",
                        accountTX.getUpdatedAt()
                ));
            } else if (accountTX.getType().equals("market") || accountTX.getType().equals("dalle")) {
                User sender = userRepository.getById(accountTX.getSenderId());
                User receiver = userRepository.getById(accountTX.getReceiverId());
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),        // sender 가 보낸 양
                        utilService.doubleToString(accountTX.getPay2()),        // receiver 가 받은 양
                        "",
                        sender.getNickname(),
                        sender.getAccountAddress(),
                        receiver.getNickname(),
                        receiver.getAccountAddress(),
                        accountTX.getUpdatedAt()
                ));
            } else if (accountTX.getType().equals("mindalle") || accountTX.getType().equals("upload")) {
                User sender = userRepository.getById(accountTX.getSenderId());
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),
                        "",
                        "",
                        sender.getNickname(),
                        sender.getAccountAddress(),
                        "",
                        "",
                        accountTX.getUpdatedAt()
                ));
            } else if (accountTX.getType().equals("NftOut")) {
                User sender = userRepository.getById(accountTX.getSenderId());
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),
                        "",
                        "",
                        sender.getNickname(),
                        sender.getAccountAddress(),
                        "",
                        accountTX.getReceiverAccountAddress(),
                        accountTX.getUpdatedAt()
                ));
            } else if (accountTX.getType().equals("klayOut")) {
                User sender = userRepository.getById(accountTX.getSenderId());
                if (accountTX.getReceiverId() == null) {
                    getAccountTxRes.add(new GetAccountTxRes(
                            accountTX.getType(),
                            utilService.doubleToString(accountTX.getPay()),
                            "",
                            "",
                            sender.getNickname(),
                            sender.getAccountAddress(),
                            "",
                            accountTX.getReceiverAccountAddress(),
                            accountTX.getUpdatedAt()
                    ));
                } else {
                    User receiver = userRepository.getById(accountTX.getReceiverId());
                    getAccountTxRes.add(new GetAccountTxRes(
                            accountTX.getType(),
                            utilService.doubleToString(accountTX.getPay()),
                            "",
                            "",
                            sender.getNickname(),
                            sender.getAccountAddress(),
                            receiver.getNickname(),
                            receiver.getAccountAddress(),
                            accountTX.getUpdatedAt()
                    ));
                }
            } else if (accountTX.getType().equals("LpSwap1")) {
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),
                        utilService.doubleToString(accountTX.getPay2()),
                        utilService.doubleToString(accountTX.getLp()),
                        userRepository.getById(accountTX.getSenderId()).getNickname(),
                        userRepository.getById(accountTX.getSenderId()).getAccountAddress(),
                        "",
                        "",
                        accountTX.getUpdatedAt()
                ));
            } else if (accountTX.getType().equals("LpSwap2")) {
                getAccountTxRes.add(new GetAccountTxRes(
                        accountTX.getType(),
                        utilService.doubleToString(accountTX.getPay()),
                        utilService.doubleToString(accountTX.getPay2()),
                        utilService.doubleToString(accountTX.getLp()),
                        "",
                        "",
                        userRepository.getById(accountTX.getReceiverId()).getNickname(),
                        userRepository.getById(accountTX.getReceiverId()).getAccountAddress(),
                        accountTX.getUpdatedAt()
                ));
            }
        }
        return getAccountTxRes;
    }

}
