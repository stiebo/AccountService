package account.domain.dto;

import java.util.List;

public class GetEmplPayrollResponseWrapperDto {
    private List<GetPayrollResponseDto> payrolls;

    public GetEmplPayrollResponseWrapperDto(List<GetPayrollResponseDto> payrolls) {
        this.payrolls = payrolls;
    }

    public List<GetPayrollResponseDto> getPayrolls() {
        return payrolls;
    }

    public void setPayrolls(List<GetPayrollResponseDto> payrolls) {
        this.payrolls = payrolls;
    }
}
