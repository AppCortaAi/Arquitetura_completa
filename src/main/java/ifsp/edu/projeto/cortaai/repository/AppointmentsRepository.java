package ifsp.edu.projeto.cortaai.repository;

import ifsp.edu.projeto.cortaai.model.Appointments;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus;
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

    /**
     * Encontra todos os agendamentos de uma barbearia.
     * Útil para a visualização do Dono (Owner).
     */
    List<Appointments> findByBarbershopId(UUID barbershopId);


    /**
     * Método para encontrar agendamentos de um barbeiro em um intervalo de tempo (um dia específico)
     * Útil para o agendamento
     */
    List<Appointments> findByBarberIdAndStartTimeBetween(UUID barberId, OffsetDateTime startOfDay, OffsetDateTime endOfDay);

    /**
     * Verifica se existem agendamentos com status 'SCHEDULED' para uma barbearia.
     * @param barbershopId O ID da barbearia.
     * @return true se houver agendamentos agendados, false caso contrário.
     */
    boolean existsByBarbershopIdAndStatus(UUID barbershopId, AppointmentStatus status);

    /**
     * Verifica se existe algum agendamento com um status específico que contenha uma determinada atividade.
     * @param activityId O ID da atividade a ser verificada.
     * @param status O status do agendamento (ex: SCHEDULED).
     * @return true se existir, false caso contrário.
     */
    boolean existsByActivitiesIdAndStatus(UUID activityId, AppointmentStatus status);
}