package account.domain.dto;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.regex.PatternSyntaxException;

@Component
public class YearMonthConverter implements Converter<String, YearMonth> {

    @Override
    public YearMonth convert(String value)
            throws PatternSyntaxException, NumberFormatException, DateTimeException {
        String[] parts = value.split("-");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return YearMonth.of(year, month);
    }
}
