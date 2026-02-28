package account.mapper;

import account.business.AdminService;
import account.business.exception.EmployeeNotFoundException;
import account.domain.dto.GetPayrollResponseDto;
import account.domain.dto.UploadPayrollDto;
import account.domain.entities.Payroll;
import account.domain.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollMapperTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private PayrollMapper mapper;

    @Test
    void toEntity_employeeFound_returnsPayroll() {
        User user = new User();
        user.setEmail("emp@acme.com");
        user.setUsername("emp@acme.com");
        YearMonth period = YearMonth.of(2023, 1);
        UploadPayrollDto dto = new UploadPayrollDto("emp@acme.com", period, 150000L);
        when(adminService.getUserByUsername("emp@acme.com")).thenReturn(Optional.of(user));

        Payroll payroll = mapper.toEntity(dto);

        assertEquals(period, payroll.getPeriod());
        assertEquals(150000L, payroll.getSalary());
        assertEquals(user, payroll.getUser());
    }

    @Test
    void toEntity_employeeNotFound_throwsEmployeeNotFoundException() {
        YearMonth period = YearMonth.of(2023, 1);
        UploadPayrollDto dto = new UploadPayrollDto("unknown@acme.com", period, 100000L);
        when(adminService.getUserByUsername("unknown@acme.com")).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> mapper.toEntity(dto));
    }

    @Test
    void toGetPayrollDto_formatsCorrectly() {
        User user = new User();
        user.setName("Alice");
        user.setLastname("Wonder");
        Payroll payroll = new Payroll();
        payroll.setUser(user);
        payroll.setPeriod(YearMonth.of(2023, 6));
        payroll.setSalary(123456L);

        GetPayrollResponseDto dto = mapper.toGetPayrollDto(payroll);

        assertEquals("Alice", dto.name());
        assertEquals("Wonder", dto.lastname());
        assertEquals(YearMonth.of(2023, 6), dto.period());
        assertEquals("1234 dollar(s) 56 cent(s)", dto.salary());
    }

    @Test
    void toGetPayrollDto_exactDollars_showsZeroCents() {
        User user = new User();
        user.setName("Bob");
        user.setLastname("Builder");
        Payroll payroll = new Payroll();
        payroll.setUser(user);
        payroll.setPeriod(YearMonth.of(2024, 1));
        payroll.setSalary(100000L);

        GetPayrollResponseDto dto = mapper.toGetPayrollDto(payroll);

        assertEquals("1000 dollar(s) 0 cent(s)", dto.salary());
    }
}
