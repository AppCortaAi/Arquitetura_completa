package ifsp.edu.projeto.cortaai.repository;

import ifsp.edu.projeto.cortaai.model.Activity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    /**

     Encontra todos os serviços associados a uma barbearia específica.
     Útil para o "Fluxo 4: Agendamento pelo Cliente".*/
    List<Activity> findByBarbershopId(UUID barbershopId);

}
