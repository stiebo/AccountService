package account.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.YearMonth;

public record GetPayrollResponseDto(
        String name,
        String lastname,
        @JsonSerialize(using = YearMonthSerializer.class)
        YearMonth period,
        String salary) { }

