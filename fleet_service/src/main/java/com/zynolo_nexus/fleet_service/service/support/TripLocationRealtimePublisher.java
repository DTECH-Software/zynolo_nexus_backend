package com.zynolo_nexus.fleet_service.service.support;

import com.zynolo_nexus.fleet_service.config.WebSocketConfig;
import com.zynolo_nexus.fleet_service.dto.ws.TripLocationWsEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripLocationRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(TripLocationWsEvent event) {
        if (event == null || event.getTripId() == null) {
            return;
        }
        String destination = WebSocketConfig.tripLocationTopic(event.getTripId());
        try {
            messagingTemplate.convertAndSend(destination, event);
        } catch (Exception ex) {
            log.warn("Failed to publish trip location WS event for tripId={}: {}", event.getTripId(), ex.getMessage());
        }
    }
}