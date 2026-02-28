package account.domain.dto;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class YearMonthConverterTest {

    private final YearMonthConverter converter = new YearMonthConverter();

    @Test
    void convert_validInput_returnsYearMonth() {
        YearMonth result = converter.convert("01-2023");
        assertEquals(YearMonth.of(2023, 1), result);
    }

    @Test
    void convert_december_returnsCorrectYearMonth() {
        YearMonth result = converter.convert("12-2024");
        assertEquals(YearMonth.of(2024, 12), result);
    }
}
