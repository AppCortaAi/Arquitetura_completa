package ifsp.edu.projeto.cortaai.model;

import ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "appointments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Appointments {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ONDE (a loja)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    // COM QUEM (o barbeiro)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    // QUEM (o cliente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @CreatedDate
    @Column(name = "date_created", nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    // Relacionamento: N Agendamentos contêm N Serviços
    @ManyToMany
    @JoinTable(
            name = "appointment_activities",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    private Set<Activity> activities;
}