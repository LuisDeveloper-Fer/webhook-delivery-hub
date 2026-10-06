package dev.portfolio.api;
import dev.portfolio.application.DeliveryService;
import dev.portfolio.domain.Delivery;
import dev.portfolio.infrastructure.DeliveryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.List;
@RestController @RequestMapping("/api") public class WebhookController {
 private final DeliveryService service;private final DeliveryRepository repo;
 public WebhookController(DeliveryService service,DeliveryRepository repo){this.service=service;this.repo=repo;}
 public record Input(@NotBlank @Pattern(regexp="[a-z.]{1,40}") String type,@NotBlank @Size(max=512) String message){}
 @PostMapping("/events") public ResponseEntity<Delivery> publish(@Valid @RequestBody Input input){var d=service.publish(input.type(),input.message());return ResponseEntity.accepted().location(URI.create("/api/deliveries/"+d.id)).body(d);}
 @GetMapping("/deliveries") public List<Delivery> list(){return repo.findAll(org.springframework.data.domain.PageRequest.of(0,50,org.springframework.data.domain.Sort.by("createdAt").descending())).getContent();}
 @GetMapping("/deliveries/{id}") public Delivery get(@PathVariable String id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PostMapping("/deliveries/{id}/replay") public Delivery replay(@PathVariable String id){return service.replay(id);}
}
