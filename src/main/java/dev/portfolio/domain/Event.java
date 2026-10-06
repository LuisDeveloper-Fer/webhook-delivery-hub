package dev.portfolio.domain;
import jakarta.persistence.*;
@Entity @Table(name="business_event") public class Event {
 @Id public String id;public String type;
 protected Event(){}public Event(String id,String type){this.id=id;this.type=type;}
}
