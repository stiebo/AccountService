package account.business.impl;

import account.business.exception.AccountServiceException;
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
class AccountantServiceImplTest {

    @Mock
    private PayrollRepository repository;

    @InjectMocks
    private AccountantServiceImpl service;

    @Test
    void savePayrolls_validPayrolls_savesAll() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        Payroll payroll = new Payroll().setUser(user).setPeriod(period).setSalary(100000L);
        when(repository.existsByUserAndPeriod(user, period)).thenReturn(false);

        service.savePayrolls(List.of(payroll));

        verify(repository).save(payroll);
    }

    @Test
    void savePayrolls_negativeSalary_throwsAccountServiceException() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        Payroll payroll = new Payroll().setUser(user).setPeriod(period).setSalary(-1L);

        AccountServiceException ex = assertThrows(AccountServiceException.class,
                () -> service.savePayrolls(List.of(payroll)));
        assertEquals("Salary must be non negative!", ex.getMessage());
    }

    @Test
    void savePayrolls_duplicatePayroll_throwsAccountServiceException() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        Payroll payroll = new Payroll().setUser(user).setPeriod(period).setSalary(100000L);
        when(repository.existsByUserAndPeriod(user, period)).thenReturn(true);

        AccountServiceException ex = assertThrows(AccountServiceException.class,
                () -> service.savePayrolls(List.of(payroll)));
        assertEquals("Payroll for employee and period already exists!", ex.getMessage());
    }

    @Test
    void updateSalary_found_updatesSalary() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        Payroll incoming = new Payroll().setUser(user).setPeriod(period).setSalary(200000L);
        Payroll dbPayroll = new Payroll().setUser(user).setPeriod(period).setSalary(100000L);
        when(repository.findByUserAndPeriod(user, period)).thenReturn(Optional.of(dbPayroll));

        service.updateSalary(incoming);

        verify(repository).save(dbPayroll);
        assertEquals(200000L, dbPayroll.getSalary());
    }

    @Test
    void updateSalary_notFound_throwsPayrollNotFoundException() {
        User user = new User();
        YearMonth period = YearMonth.of(2023, 1);
        Payroll incoming = new Payroll().setUser(user).setPeriod(period).setSalary(200000L);
        when(repository.findByUserAndPeriod(user, period)).thenReturn(Optional.empty());

        assertThrows(PayrollNotFoundException.class, () -> service.updateSalary(incoming));
    }
}
