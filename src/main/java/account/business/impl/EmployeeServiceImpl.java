package account.business.impl;

import account.business.EmployeeService;
import account.business.exception.PayrollNotFoundException;
import account.domain.entities.Payroll;
import account.domain.entities.User;
import account.repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final PayrollRepository repository;

    @Autowired
    public EmployeeServiceImpl (PayrollRepository payrollRepository) {
        this.repository = payrollRepository;
    }

    @Override
    public Payroll getPayroll (User user, YearMonth period) throws PayrollNotFoundException {
        return repository.findByUserAndPeriod(user, period)
                .orElseThrow(PayrollNotFoundException::new);
    }

    public List<Payroll> getPayrolls (User user) {
        return repository.findAllByUserOrderByPeriodDesc(user)
                .orElseThrow(PayrollNotFoundException::new);
    }
}
