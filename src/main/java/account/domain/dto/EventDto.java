package account.domain.dto;

import java.time.Instant;

public record EventDto(Instant date, String action, String subject, String object, String path) {
}
