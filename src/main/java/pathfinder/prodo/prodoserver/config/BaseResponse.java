package pathfinder.prodo.prodoserver.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BaseResponse<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private int code;

    public BaseResponse(BaseResponseStatus status) {
        this.timestamp = LocalDateTime.now();
        this.status = status.getStatus();
        this.message = status.getMessage();
        this.code = status.getCode();
    }
}
