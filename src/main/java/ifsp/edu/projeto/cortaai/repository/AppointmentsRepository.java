package ifsp.edu.projeto.cortaai.repository;

import ifsp.edu.projeto.cortaai.model.Appointments;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface AppointmentsRepository extends JpaRepository<Appointments, Long> {

    Appointments findFirstByBarberId(UUID barberId);

    Appointments findFirstByCustomerId(UUID customerId);

    /**

    Encontra todos os agendamentos de um cliente.
    Útil para o histórico do cliente.*/
    List<Appointments> findByCustomerId(UUID customerId);

    /**

    Encontra todos os agendamentos de um barbeiro.
    Útil para o histórico do barbeiro.*/
    List<Appointments> findByBarberId(UUID barberId);

    /**

     Encontra agendamentos de um barbeiro que se sobrepõem a um determinado
     intervalo de tempo.
     Essencial para o "Fluxo 4: Cálculo de Disponibilidade".*
     Esta query verifica conflitos:
     O novo agendamento começa durante um existente.
     O novo agendamento termina durante um existente.
     O novo agendamento "envolve" um existente.
     */
    @Query("SELECT a FROM Appointments a WHERE a.barber.id = ?1 AND " +"(a.startTime < ?3 AND a.endTime > ?2) AND " +"a.status <> 'CANCELLED'")
    List<Appointments> findConflictingAppointments(UUID barberId, OffsetDateTime newStartTime, OffsetDateTime newEndTime);
}