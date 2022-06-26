package pathfinder.prodo.prodoserver.appManage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pathfinder.prodo.prodoserver.appManage.Dto.SendFcmDto;
import pathfinder.prodo.prodoserver.fcm.FcmService;
import pathfinder.prodo.prodoserver.user.UserRepository;
import pathfinder.prodo.prodoserver.user.VO.User;
import pathfinder.prodo.prodoserver.utils.mail.MailService;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppManageService {

    private final FcmService fcmService;
    private final MailService mailService;
    private final UserRepository userRepository;

    public void sendAllUsersFcm(SendFcmDto sendEmailDto) throws IOException {
        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            fcmService.sendMessageTo(user.getDeviceToken(), 0L, null, sendEmailDto.getTitle(), sendEmailDto.getDescription());
        }
    }

    public void sendAllUsersEmail(SendFcmDto sendFcmDto) {
        List<User> users = userRepository.findAll();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            mailService.sendAllUsersEmail(user.getEmail(), sendFcmDto.getTitle(), sendFcmDto.getDescription());
            System.out.println(user.getEmail());
        }
    }

}
