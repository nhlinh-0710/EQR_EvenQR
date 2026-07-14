package com.eventqr.service;

import com.eventqr.model.Event;
import com.eventqr.dto.EventRequest;
import java.util.List;

public interface EventService {
    Event createEvent(EventRequest request) throws Exception;
    Event updateEvent(Long eventId, EventRequest request, Long organizerId) throws Exception;
    Event getEventById(Long eventId); 
    void deleteEvent(Long eventId, Long organizerId) throws Exception;

    List<Event> findAll();
    List<Event> findByOrganizerId(Long organizerId);
    
    // Kiểm tra quyền sở hữu
    boolean isEventOwner(Long eventId, Long organizerId);
}