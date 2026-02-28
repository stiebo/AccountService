package account.mapper;

import account.domain.dto.EventDto;
import account.domain.entities.Event;
import account.business.EventName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SecurityMapperTest {

    private final SecurityMapper mapper = new SecurityMapper();

    @Test
    void toDto_convertsEventToEventDto() {
        Instant now = Instant.now();
        Event event = new Event()
                .setDate(now)
                .setAction(EventName.CREATE_USER)
                .setSubject("anonymous")
                .setObject("user@acme.com")
                .setPath("/api/auth/signup");

        EventDto dto = mapper.toDto(event);

        assertEquals(now, dto.date());
        assertEquals("CREATE_USER", dto.action());
        assertEquals("anonymous", dto.subject());
        assertEquals("user@acme.com", dto.object());
        assertEquals("/api/auth/signup", dto.path());
    }
}
