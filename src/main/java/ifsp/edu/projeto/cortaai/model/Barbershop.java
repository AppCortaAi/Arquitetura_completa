package ifsp.edu.projeto.cortaai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "barbershops")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Barbershop {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(length = 255)
    private String address;

    @CreatedDate
    @Column(name = "date_created", nullable = false, updatable = false)
    private OffsetDateTime dateCreated;

    @LastModifiedDate
    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    // Relacionamento: 1 Barbearia tem N Barbeiros
    @OneToMany(mappedBy = "barbershop")
    private Set<Barber> barbers;

    // Relacionamento: 1 Barbearia tem N Serviços
    @OneToMany(mappedBy = "barbershop")
    private Set<Activity> activities;

    // Relacionamento: 1 Barbearia tem N Agendamentos
    @OneToMany(mappedBy = "barbershop")
    private Set<Appointments> appointments;

    // Relacionamento: 1 Barbearia tem N Pedidos para Entrar
    @OneToMany(mappedBy = "barbershop")
    private Set<BarbershopJoinRequest> joinRequests;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "banner_url", length = 255)
    private String bannerUrl;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BarbershopHighlight> highlights;
}
