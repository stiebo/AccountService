package account.business.impl;

import account.business.exception.PayrollNotFoundException;
import account.domain.entities.Payroll;
import account.domain.entities.User;
import account.repository.PayrollRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private PayrollRepository repository;

    @InjectMocks
    private EmployeeServiceImpl service;

    @Test
    void getPayroll_found_returnsPayroll() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        Payroll payroll = new Payroll();
        when(repository.findByUserAndPeriod(user, period)).thenReturn(Optional.of(payroll));

        Payroll result = service.getPayroll(user, period);

        assertEquals(payroll, result);
    }

    @Test
    void getPayroll_notFound_throwsPayrollNotFoundException() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        when(repository.findByUserAndPeriod(user, period)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () -> service.getPayroll(user, period));
    }

    @Test
    void getPayrolls_found_returnsPayrollList() {
        User user = new User();
        List<Payroll> payrolls = List.of(new Payroll(), new Payroll());
        when(repository.findAllByUserOrderByPeriodDesc(user)).thenReturn(Optional.of(payrolls));

        List<Payroll> result = service.getPayrolls(user);

        assertEquals(2, result.size());
    }

    @Test
    void getPayrolls_notFound_throwsPayrollNotFoundException() {
        User user = new User();
        when(repository.findAllByUserOrderByPeriodDesc(user)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () -> service.getPayrolls(user));
    }
}
