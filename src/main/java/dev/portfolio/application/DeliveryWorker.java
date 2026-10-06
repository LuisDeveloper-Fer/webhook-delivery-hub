package dev.portfolio.application;

import dev.portfolio.domain.Delivery;
import dev.portfolio.infrastructure.*;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@EnableScheduling
public class DeliveryWorker {
  private final DeliveryRepository repo;
  private final TransactionTemplate tx;
  private final URI destination;
  private final String secret;
  private final MeterRegistry metrics;
  private final HttpClient http =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofMillis(500))
          .followRedirects(HttpClient.Redirect.NEVER)
          .build();

  public DeliveryWorker(
      DeliveryRepository repo,
      TransactionTemplate tx,
      @Value("${receiver.url:http://localhost:9092/events}") String destination,
      @Value("${webhook.secret:local-demo-key-change-me}") String secret,
      MeterRegistry metrics) {
    this.repo = repo;
    this.tx = tx;
    this.destination = URI.create(destination);
    this.secret = secret;
    this.metrics = metrics;
  }

  @Scheduled(
      fixedDelayString = "${worker.delay:1000}",
      initialDelayString = "${worker.initial-delay:1000}")
  public synchronized void tick() {
    for (var candidate : repo.due(Instant.now(), PageRequest.of(0, 10))) {
      Delivery d =
          tx.execute(
              status -> {
                var row = repo.locked(candidate.id).orElseThrow();
                if (!(row.status.equals("PENDING") || row.status.equals("SENDING"))
                    || row.nextAttempt.isAfter(Instant.now())) return null;
                if (row.attempts >= 5) {
                  row.status = "FAILED";
                  return null;
                }
                row.status = "SENDING";
                row.attempts++;
                row.nextAttempt = Instant.now().plusSeconds(30);
                return row;
              });
      if (d == null) continue;
      int code;
      String timestamp = Long.toString(Instant.now().getEpochSecond());
      try {
        var request =
            HttpRequest.newBuilder(destination)
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .header("X-Event-ID", d.id)
                .header("X-Timestamp", timestamp)
                .header("X-Signature", Signer.sign(secret, timestamp, d.payload))
                .POST(HttpRequest.BodyPublishers.ofString(d.payload))
                .build();
        code = http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (Exception e) {
        code = 0;
      }
      final int result = code;
      tx.executeWithoutResult(
          status -> {
            var row = repo.locked(d.id).orElseThrow();
            row.history +=
                "Attempt "
                    + row.attempts
                    + ": "
                    + (result == 0 ? "TIMEOUT_OR_CONNECTION_ERROR" : result)
                    + "\n";
            if (result >= 200 && result < 300) row.status = "DELIVERED";
            else if (row.attempts >= 5 || (result >= 400 && result < 500 && result != 429))
              row.status = "FAILED";
            else {
              row.status = "PENDING";
              row.nextAttempt = Instant.now().plusMillis(backoffMillis(row.attempts));
            }
          });
      metrics
          .counter(
              "webhooks.attempts", "outcome", result >= 200 && result < 300 ? "success" : "failure")
          .increment();
    }
  }

  public static long backoffMillis(int attempt) {
    return Math.min(30000, 1000L << Math.min(attempt - 1, 5))
        + ThreadLocalRandom.current().nextLong(250);
  }
}
