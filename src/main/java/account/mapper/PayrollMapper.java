package account.mapper;

import account.business.AdminService;
import account.domain.dto.GetPayrollResponseDto;
import account.domain.dto.UploadPayrollDto;
import account.business.exception.EmployeeNotFoundException;
import account.domain.entities.Payroll;
import account.domain.entities.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PayrollMapper {
    private final AdminService adminService;

    @Autowired
    public PayrollMapper (AdminService adminService) {
        this.adminService = adminService;
    }

    public Payroll toEntity (UploadPayrollDto dto) throws UsernameNotFoundException {
        Optional<User> optionalUser = adminService.getUserByUsername(dto.employee());
        if (optionalUser.isEmpty()) {
            throw new EmployeeNotFoundException();
        }
        return new Payroll()
                .setPeriod(dto.period())
                .setSalary(dto.salary())
                .setUser(optionalUser.get());
    }

    public GetPayrollResponseDto toGetPayrollDto (Payroll payroll) {
        return new GetPayrollResponseDto(payroll.getUser().getName(), payroll.getUser().getLastname(),
                payroll.getPeriod(), "%d dollar(s) %d cent(s)".formatted(payroll.getSalary()/100,
                payroll.getSalary()%100));
    }
}
