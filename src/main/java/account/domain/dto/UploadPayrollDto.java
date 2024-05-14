package account.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

public record UploadPayrollDto(
        @NotBlank
        String employee,
        @NotNull
        @JsonDeserialize(using = YearMonthDeserializer.class)
        YearMonth period,
        @NotNull
        Long salary) {
}

