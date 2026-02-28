package account.domain.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class YearMonthDeserializerTest {

    private final YearMonthDeserializer deserializer = new YearMonthDeserializer();

    @Test
    void deserialize_validInput_returnsYearMonth() throws IOException {
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext ctx = mock(DeserializationContext.class);
        when(parser.getText()).thenReturn("01-2023");

        YearMonth result = deserializer.deserialize(parser, ctx);

        assertEquals(YearMonth.of(2023, 1), result);
    }
}
