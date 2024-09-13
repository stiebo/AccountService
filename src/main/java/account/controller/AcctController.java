package account.controller;

import account.business.AccountantService;
import account.domain.dto.UploadPayrollDto;
import account.mapper.PayrollMapper;
import account.domain.entities.Payroll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acct")
public class AcctController {
    private final AccountantService service;
    private final PayrollMapper mapper;

    @Autowired
    public AcctController(AccountantService accountantService, PayrollMapper payrollMapper) {
        this.service = accountantService;
        this.mapper = payrollMapper;
    }

    @PostMapping("/payments")
    public Map<String, String> uploadPayrolls(@Valid @RequestBody List<UploadPayrollDto> uploadPayrollDtoList) {
        List<Payroll> payrollList = new ArrayList<>();
        uploadPayrollDtoList.forEach(uploadPayrollDto -> payrollList.add(mapper.toEntity(uploadPayrollDto)));
        service.savePayrolls(payrollList);
        return Collections.singletonMap("status", "Added successfully!");
    }

    @PutMapping("/payments")
    public Map<String, String> updateSalary(@Valid @RequestBody UploadPayrollDto uploadPayrollDto) {
        service.updateSalary(mapper.toEntity(uploadPayrollDto));
        return Collections.singletonMap("status", "Updated successfully!");
    }
}
