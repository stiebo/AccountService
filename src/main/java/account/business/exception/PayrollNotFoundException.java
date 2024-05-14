package account.business.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PayrollNotFoundException extends RuntimeException {
    public PayrollNotFoundException() {
        super("Payroll not found!");
    }
}
