package dev.portfolio;

import static org.assertj.core.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import dev.portfolio.application.*;
import dev.portfolio.infrastructure.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(properties = "worker.initial-delay=3600000")
class WebhookTest {
  static HttpServer server;
  static AtomicInteger reply = new AtomicInteger(200);
  static String signature, body, timestamp;

  static {
    try {
      server = HttpServer.create(new InetSocketAddress(0), 0);
      server.createContext(
          "/events",
          exchange -> {
            body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            signature = exchange.getRequestHeaders().getFirst("X-Signature");
            timestamp = exchange.getRequestHeaders().getFirst("X-Timestamp");
            exchange.sendResponseHeaders(reply.get(), -1);
            exchange.close();
          });
      server.start();
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("receiver.url", () -> "http://localhost:" + server.getAddress().getPort() + "/events");
  }

  @Autowired DeliveryService service;
  @Autowired DeliveryWorker worker;
  @Autowired DeliveryRepository repo;

  @AfterAll
  static void close() {
    server.stop(0);
  }

  @Test
  void outboxDeliversSignedBody() {
    reply.set(200);
    var d = service.publish("payment.approved", "fictional");
    worker.tick();
    assertThat(repo.findById(d.id).orElseThrow().status).isEqualTo("DELIVERED");
    assertThat(signature).isEqualTo(Signer.sign("local-demo-key-change-me", timestamp, body));
  }

  @Test
  void failureSchedulesRetryAndReplayRequiresExhaustion() {
    reply.set(500);
    var d = service.publish("payment.approved", "fictional");
    worker.tick();
    var failed = repo.findById(d.id).orElseThrow();
    assertThat(failed.status).isEqualTo("PENDING");
    assertThat(failed.attempts).isEqualTo(1);
    assertThatThrownBy(() -> service.replay(d.id)).hasMessageContaining("409");
  }

  @Test
  void backoffIsBounded() {
    assertThat(DeliveryWorker.backoffMillis(1)).isBetween(1000L, 1249L);
    assertThat(DeliveryWorker.backoffMillis(5)).isBetween(16000L, 16249L);
  }
}
