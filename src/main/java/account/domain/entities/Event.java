package account.domain.entities;

import account.business.EventName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.Instant;

// better to not use data on entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
// it is ok to have the table called event.
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private Instant date;

    @Column(nullable = false)
    private EventName action;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String object;

    @Column(nullable = false)
    private String path;
}
