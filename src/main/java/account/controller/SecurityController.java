package account.controller;

import account.business.SecurityService;
import account.domain.dto.EventDto;
import account.mapper.SecurityMapper;
import account.domain.entities.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/security")
public class SecurityController {
    private final SecurityService service;
    private final SecurityMapper mapper;

    @Autowired
    public SecurityController (SecurityService securityService, SecurityMapper securityMapper) {
        this.service = securityService;
        this.mapper = securityMapper;
    }

    @GetMapping("/events/")
    public ResponseEntity<EventDto[]> readSecurityEvents () {
        List<Event> events = service.getAllEvents();
        return ResponseEntity.ok(events.stream()
                .map(mapper::toDto)
                .toArray(EventDto[]::new));
    }

}
