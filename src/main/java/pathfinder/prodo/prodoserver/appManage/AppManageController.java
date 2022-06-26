package pathfinder.prodo.prodoserver.appManage;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pathfinder.prodo.prodoserver.appManage.Dto.SendFcmDto;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AppManageController {
    private final AppManageService appManageService;

    /**
     * 전체 푸쉬메세지 발송
     * [POST] /all/fcm
     */
    @PostMapping("/all/fcm")
    public void sendAllUserFCM(@RequestBody SendFcmDto sendEmailDto) throws IOException {
        appManageService.sendAllUsersFcm(sendEmailDto);
    }

    /**
     * 전체 이메일 발송
     * [POST] /all/email
     */
    @PostMapping("/all/email")
    public void sendALlUserEmail(@RequestBody SendFcmDto sendFcmDto) {
        appManageService.sendAllUsersEmail(sendFcmDto);
    }
}
