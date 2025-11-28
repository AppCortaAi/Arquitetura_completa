package ifsp.edu.projeto.cortaai.repository;

import ifsp.edu.projeto.cortaai.model.Barbershop;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopRepository extends JpaRepository<Barbershop, UUID> {

    /**

     Encontra uma barbearia pelo seu CNPJ.
     Essencial para o "Fluxo 2: Entrada de Barbeiro".*/
    Optional<Barbershop> findByCnpj(String cnpj);

}