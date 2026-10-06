package dev.portfolio.infrastructure;
import dev.portfolio.domain.Delivery;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;
public interface DeliveryRepository extends JpaRepository<Delivery,String>{
 @Query("select d from Delivery d where (d.status='PENDING' or d.status='SENDING') and d.nextAttempt<=:now order by d.nextAttempt") List<Delivery> due(Instant now,org.springframework.data.domain.Pageable page);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from Delivery d where d.id=:id") Optional<Delivery> locked(String id);
}
