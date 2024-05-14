package account.mapper;

import account.domain.dto.EventDto;
import account.domain.entities.Event;
import org.springframework.stereotype.Component;

@Component
public class SecurityMapper {
    public EventDto toDto (Event event) {
        return new EventDto(
                event.getDate(),
                event.getAction().toString(),
                event.getSubject(),
                event.getObject(),
                event.getPath());
    }
}
