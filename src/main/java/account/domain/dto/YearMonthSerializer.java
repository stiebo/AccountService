package account.domain.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class YearMonthSerializer extends JsonSerializer<YearMonth> {

        @Override
        public void serialize(YearMonth yearMonth, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                String formattedYearMonth = yearMonth.getMonth().getDisplayName(
                        TextStyle.FULL,
                        Locale.US
                ) + "-" + yearMonth.getYear();

                jsonGenerator.writeString(formattedYearMonth);
        }
}
