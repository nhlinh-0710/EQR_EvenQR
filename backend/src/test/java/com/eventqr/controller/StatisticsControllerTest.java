package com.eventqr.controller;

import com.eventqr.dto.EventStatisticsDTO;
import com.eventqr.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController controller;

    @Test
    void getOrganizerStatistics_shouldReturnStats() {
        EventStatisticsDTO stats = new EventStatisticsDTO(1L, 5, 100);
        when(statisticsService.getOrganizerStatistics(1L)).thenReturn(stats);

        var response = controller.getOrganizerStatistics(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(stats, response.getBody());
    }

    @Test
    void getOrganizerStatistics_whenIllegalArgument_shouldReturnBadRequest() {
        when(statisticsService.getOrganizerStatistics(1L)).thenThrow(new IllegalArgumentException("invalid"));

        var response = controller.getOrganizerStatistics(1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void getOrganizerStatistics_whenException_shouldReturn500() {
        when(statisticsService.getOrganizerStatistics(1L)).thenThrow(new RuntimeException("error"));

        var response = controller.getOrganizerStatistics(1L);

        assertTrue(response.getStatusCode().is5xxServerError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }
}
