package ifsp.edu.projeto.cortaai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Entity
@Table(name = "barbershop_highlights")
@Getter
@Setter
public class BarbershopHighlight {

    @Id
    @Column(nullable = false, updatable = false, length = 36)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    // (Tornamos nullable=true pois pode haver imagens antigas sem publicId)
    @Column(name = "image_url_public_id", length = 255) // NOVA COLUNA
    private String imageUrlPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;
}