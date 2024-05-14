package account.mapper;

import account.domain.dto.GetPayrollResponseDto;
import account.domain.dto.UploadPayrollDto;
import account.business.exception.EmployeeNotFoundException;
import account.domain.entities.Payroll;
import account.domain.entities.User;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class PayrollMapper {
    private final UserRepository userRepository;

    @Autowired
    public PayrollMapper (UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Payroll toEntity (UploadPayrollDto dto) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(dto.employee())
                .orElseThrow(EmployeeNotFoundException::new);
        return new Payroll()
                .setPeriod(dto.period())
                .setSalary(dto.salary())
                .setUser(user);
    }

    public GetPayrollResponseDto toGetPayrollDto (Payroll payroll) {
        return new GetPayrollResponseDto(payroll.getUser().getName(), payroll.getUser().getLastname(),
                payroll.getPeriod(), "%d dollar(s) %d cent(s)".formatted(payroll.getSalary()/100,
                payroll.getSalary()%100));
    }
}
