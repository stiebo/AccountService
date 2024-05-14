package account.business.impl;

import account.business.AccountantService;
import account.business.exception.AccountServiceException;
import account.business.exception.PayrollNotFoundException;
import account.domain.entities.Payroll;
import account.repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountantServiceImpl implements AccountantService {
    private final PayrollRepository repository;

    @Autowired
    public AccountantServiceImpl(PayrollRepository payrollRepository) {
        this.repository = payrollRepository;
    }

    @Override
    @Transactional
    public void savePayrolls(List<Payroll> payrollList) throws AccountServiceException {
        payrollList.stream()
                .peek(payroll -> {
                    if (payroll.getSalary() < 0) {
                        throw new AccountServiceException("Salary must be non negative!");
                    }
                    if (repository.existsByUserAndPeriod(payroll.getUser(), payroll.getPeriod())) {
                        throw new AccountServiceException("Payroll for employee and period already exists!");
                    }
                })
                .forEach(repository::save);
    }

    @Override
    @Transactional
    public void updateSalary(Payroll payroll) {
        Payroll dbPayroll = repository.findByUserAndPeriod(payroll.getUser(), payroll.getPeriod())
                .orElseThrow(PayrollNotFoundException::new);
        dbPayroll.setSalary(payroll.getSalary());
        repository.save(dbPayroll);
    }
}
