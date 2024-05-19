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

    // should return statuscode 201
    @PostMapping("/payments")
    // don't need to return response entity it is a rest controller
    public ResponseEntity<Map<String, String>> uploadPayrolls(@Valid @RequestBody
                                                                  List<UploadPayrollDto> uploadPayrollDtoList) {
        // try to have all the mapping in the mapper
        List<Payroll> payrollList = new ArrayList<>();
        uploadPayrollDtoList.forEach(uploadPayrollDto -> payrollList.add(mapper.toEntity(uploadPayrollDto)));
        service.savePayrolls(payrollList);
        return ResponseEntity.ok(Collections.singletonMap("status", "Added successfully!"));
    }

    @PutMapping("/payments")
    public ResponseEntity<Map<String, String>> updateSalary(@Valid @RequestBody UploadPayrollDto uploadPayrollDto) {
        service.updateSalary(mapper.toEntity(uploadPayrollDto));
        return ResponseEntity.ok(Collections.singletonMap("status", "Updated successfully!"));
    }
}
