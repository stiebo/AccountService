package account.business.impl;

import account.business.EventName;
import account.domain.entities.Event;
import account.repository.SecurityEventRespository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private SecurityEventRespository repository;

    @InjectMocks
    private SecurityServiceImpl service;

    @Test
    void getAllEvents_returnsEventsFromRepository() {
        Event event = new Event();
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(event));

        List<Event> events = service.getAllEvents();

        assertEquals(1, events.size());
        verify(repository).findAllByOrderByIdAsc();
    }

    @Test
    void logEvent_savesEventWithCorrectFields() {
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);

        service.logEvent(EventName.CREATE_USER, "anonymous", "user@acme.com", "/api/auth/signup");

        verify(repository).save(captor.capture());
        Event saved = captor.getValue();
        assertNotNull(saved.getDate());
        assertEquals(EventName.CREATE_USER, saved.getAction());
        assertEquals("anonymous", saved.getSubject());
        assertEquals("user@acme.com", saved.getObject());
        assertEquals("/api/auth/signup", saved.getPath());
    }
}
