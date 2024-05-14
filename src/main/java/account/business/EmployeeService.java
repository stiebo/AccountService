package account.business;

import account.domain.entities.Payroll;
import account.domain.entities.User;

import java.time.YearMonth;
import java.util.List;

public interface EmployeeService {
    Payroll getPayroll(User user, YearMonth period);
    List<Payroll> getPayrolls(User user);
}
