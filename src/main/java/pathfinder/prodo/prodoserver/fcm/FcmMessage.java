package pathfinder.prodo.prodoserver.fcm;

import lombok.*;

import java.util.List;
import java.util.Map;

@Builder @Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FcmMessage {
    private boolean validate_only;
    private Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Message {
        private Notification notification;
        private String token;
        private Map data;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Notification {
        private String title;
        private String body;
    }
}
