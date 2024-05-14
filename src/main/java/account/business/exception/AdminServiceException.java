package account.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AdminServiceException extends RuntimeException {
    public AdminServiceException(String message) {
        super(message);
    }
}
