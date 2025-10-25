package ifsp.edu.projeto.cortaai.repository;

import ifsp.edu.projeto.cortaai.model.Service;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, UUID> {

    /**

     Encontra todos os serviços associados a uma barbearia específica.
     Útil para o "Fluxo 4: Agendamento pelo Cliente".*/
    List<Service> findByBarbershopId(UUID barbershopId);

}
