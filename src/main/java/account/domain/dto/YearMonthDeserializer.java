package account.domain.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.YearMonth;

public class YearMonthDeserializer extends JsonDeserializer<YearMonth> {

        @Override
        public YearMonth deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                String[] parts = text.split("-");
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                return YearMonth.of(year, month);
        }
}

