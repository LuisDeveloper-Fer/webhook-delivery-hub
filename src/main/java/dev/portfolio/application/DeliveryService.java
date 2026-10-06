package dev.portfolio.application;

import dev.portfolio.domain.*;
import dev.portfolio.infrastructure.DeliveryRepository;
import jakarta.persistence.EntityManager;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

@Service
public class DeliveryService {
  private final EntityManager em;
  private final DeliveryRepository repo;

  public DeliveryService(EntityManager em, DeliveryRepository repo) {
    this.em = em;
    this.repo = repo;
  }

  @Transactional
  public Delivery publish(String type, String message) {
    var id = UUID.randomUUID().toString();
    var delivery =
        new Delivery(
            id,
            JsonMapper.builder()
                .build()
                .writeValueAsString(Map.of("eventId", id, "type", type, "message", message)));
    em.persist(new Event(id, type));
    em.persist(delivery);
    return delivery;
  }

  @Transactional
  public Delivery replay(String id) {
    var d = repo.locked(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!d.status.equals("FAILED"))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Only exhausted deliveries may be replayed");
    d.status = "PENDING";
    d.attempts = 0;
    d.nextAttempt = java.time.Instant.now();
    d.history = "Manual replay; previous cycle exhausted.\n";
    return d;
  }
}
