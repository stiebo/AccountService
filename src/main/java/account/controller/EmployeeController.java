package account.controller;

import account.business.EmployeeService;
import account.domain.dto.GetEmplPayrollResponseWrapperDto;
import account.domain.dto.GetPayrollResponseDto;
import account.mapper.PayrollMapper;
import account.domain.entities.Payroll;
import account.domain.entities.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {
    private final EmployeeService service;
    private final PayrollMapper mapper;

    @Autowired
    public EmployeeController(EmployeeService employeeService, PayrollMapper payrollMapper) {
        this.service = employeeService;
        this.mapper = payrollMapper;
    }

    @GetMapping("/payment")
    public ResponseEntity<?> getPayroll(
            @Valid @RequestParam(name = "period", required = false) Optional<YearMonth> period,
            @AuthenticationPrincipal User user) {
        if (period.isPresent()) {
            return ResponseEntity.ok(mapper.toGetPayrollDto(service.getPayroll(user, period.get())));
        } else {
            List<Payroll> payrolls = service.getPayrolls(user);
            List<GetPayrollResponseDto> payrollDtos = payrolls.stream()
                    .map(mapper::toGetPayrollDto)
                    .collect(Collectors.toList());
            GetEmplPayrollResponseWrapperDto responseWrapperDto = new GetEmplPayrollResponseWrapperDto(payrollDtos);
            return ResponseEntity.ok(responseWrapperDto);
        }
    }
}
