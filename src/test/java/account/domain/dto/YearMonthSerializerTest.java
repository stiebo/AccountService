package account.domain.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.YearMonth;

import static org.mockito.Mockito.*;

class YearMonthSerializerTest {

    private final YearMonthSerializer serializer = new YearMonthSerializer();

    @Test
    void serialize_validYearMonth_writesFormattedString() throws IOException {
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);

        serializer.serialize(YearMonth.of(2023, 1), gen, provider);

        verify(gen).writeString("January-2023");
    }

    @Test
    void serialize_december_writesFormattedString() throws IOException {
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);

        serializer.serialize(YearMonth.of(2024, 12), gen, provider);

        verify(gen).writeString("December-2024");
    }
}
