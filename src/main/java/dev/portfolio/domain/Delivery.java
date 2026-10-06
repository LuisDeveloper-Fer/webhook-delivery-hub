package dev.portfolio.domain;
import jakarta.persistence.*;
import java.time.Instant;
@Entity public class Delivery {
 @Id public String id;
 @Column(nullable=false,length=2000) public String payload;
 public String status;public int attempts;public Instant nextAttempt;public Instant createdAt;
 @Column(length=4000) public String history;
 @Version public long version;
 protected Delivery(){}
 public Delivery(String id,String payload){this.id=id;this.payload=payload;status="PENDING";attempts=0;createdAt=Instant.now();nextAttempt=createdAt;history="";}
}
