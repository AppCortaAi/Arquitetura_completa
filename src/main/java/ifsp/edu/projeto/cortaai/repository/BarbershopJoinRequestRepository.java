package ifsp.edu.projeto.cortaai.repository;

import ifsp.edu.projeto.cortaai.model.BarbershopJoinRequest;
import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopJoinRequestRepository extends JpaRepository<BarbershopJoinRequest, Long> {

    /**

    Encontra todos os pedidos pendentes para uma barbearia específica.
    Usado pelo Dono (Owner) para aprovar novas entradas.*/
    List<BarbershopJoinRequest> findByBarbershopIdAndStatus(UUID barbershopId, JoinRequestStatus status);

    /**

    Encontra um pedido específico de um barbeiro para uma barbearia.
    Útil para verificar se um pedido já foi feito.*/
    Optional<BarbershopJoinRequest> findByBarberIdAndBarbershopId(UUID barberId, UUID barbershopId);

    /**
     * Encontra todos os pedidos de um barbeiro específico.
     * Usado para o histórico do barbeiro.
     */
    List<BarbershopJoinRequest> findByBarberId(UUID barberId);

}