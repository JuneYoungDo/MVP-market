package pathfinder.prodo.prodoserver.utils.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private static final String FROM_ADDRESS = "MVP";

    public String getTmpPwd() {
        char[] numSet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        String pwd = "";
        int idx = 0;
        for (int i = 0; i < 6; i++) {
            idx = (int) (numSet.length * Math.random());
            pwd += numSet[idx];
        }
        return pwd;
    }

    public MailDto createMail(String email) {
        String pwd = getTmpPwd();
        MailDto mailDto = new MailDto(
                email,
                "MVP 마켓에서 임시비밀번호를 알려드립니다.",
                "안녕하세요. MVP 마켓입니다.\n" +
                        "사용자의 임시 결제 비밀번호 요청에 따라 비밀번호를 변경합니다.\n" +
                        "추후 마이페이지에서 본인의 결제 비밀번호로 변경하는 것을 권고드립니다.\n" +
                        "임시 결제 비밀번호는 " + pwd + " 입니다.\n" +
                        "감사합니다.",
                pwd
        );
        return mailDto;
    }

    public void sendMail(MailDto mailDto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailDto.getAddress());
        message.setFrom(FROM_ADDRESS);
        message.setSubject(mailDto.getTitle());
        message.setText(mailDto.getMessage());
        javaMailSender.send(message);
    }

    public void sendAllUsersEmail(String address, String title, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(address);
        message.setFrom(FROM_ADDRESS);
        message.setSubject(title);
        message.setText(body);
        javaMailSender.send(message);
    }
}
