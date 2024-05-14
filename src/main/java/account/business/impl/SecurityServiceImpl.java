package account.business.impl;

import account.business.EventName;
import account.business.SecurityService;
import account.domain.entities.Event;
import account.repository.SecurityEventRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SecurityServiceImpl implements SecurityService {
    private final SecurityEventRespository respository;

    @Autowired
    public SecurityServiceImpl (SecurityEventRespository securityEventRespository) {
        this.respository = securityEventRespository;
    }

    @Override
    public List<Event> getAllEvents() {
        return respository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional
    public void logEvent (EventName action, String subject, String object, String path) {
        Event event = new Event()
                .setDate(Instant.now())
                .setAction(action)
                .setSubject(subject)
                .setObject(object)
                .setPath(path);
        respository.save(event);
    }
}
