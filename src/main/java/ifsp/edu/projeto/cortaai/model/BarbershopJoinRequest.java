package ifsp.edu.projeto.cortaai.model;

import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "barbershop_join_requests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"barber_id", "barbershop_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class BarbershopJoinRequest {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private JoinRequestStatus status;

    @CreatedDate
    @Column(name = "date_created", nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

}