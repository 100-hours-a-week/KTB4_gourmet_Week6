package KTB4_gourmet_Week6.Assignment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private final int status;
    private final String error;
    private final String message;

    public ErrorResponse(HttpStatus status, String message) {
        this.status = status.value();
        this.error = status.name();
        this.message = message;
    }
}