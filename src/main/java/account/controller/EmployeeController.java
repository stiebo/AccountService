package account.controller;

import account.business.EmployeeService;
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
    // better to avoid generics
    public ResponseEntity<?> getPayroll(
            @Valid @RequestParam Optional<YearMonth> period,
            @AuthenticationPrincipal User user) {
// I would have a list with 1 element or a list. You can add a query inside the repo. so you only have one method.
        // It is common practice to not return collections but wrapper
        if (period.isPresent()) {
            return ResponseEntity.ok(mapper.toGetPayrollDto(service.getPayroll(user, period.get())));
        } else {
            List<Payroll> foundPayrolls = service.getPayrolls(user);
            return ResponseEntity.ok(foundPayrolls.stream()
                    .map(mapper::toGetPayrollDto)
                    .toArray(GetPayrollResponseDto[]::new));
        }
    }
}
