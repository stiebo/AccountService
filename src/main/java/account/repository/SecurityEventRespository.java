package account.repository;

import account.domain.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityEventRespository extends JpaRepository<Event, Long> {
    List<Event> findAllByOrderByIdAsc();
}
