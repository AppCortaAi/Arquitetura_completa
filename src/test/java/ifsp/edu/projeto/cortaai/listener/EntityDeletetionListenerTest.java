package ifsp.edu.projeto.cortaai.listener;

import ifsp.edu.projeto.cortaai.events.BeforeDeleteBarber;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteCustomer;
import ifsp.edu.projeto.cortaai.exception.ReferenceException;
import ifsp.edu.projeto.cortaai.model.Appointments;
import ifsp.edu.projeto.cortaai.repository.AppointmentsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EntityDeletionListener")
class EntityDeletionListenerTest {

    @Mock
    private AppointmentsRepository appointmentsRepository; // Dependência mocada

    @InjectMocks
    private EntityDeletionListener entityDeletionListener; // Classe sob teste

    private final UUID testBarberId = UUID.randomUUID();
    private final UUID testCustomerId = UUID.randomUUID();

    // --- Testes para BeforeDeleteBarber ---

    @Test
    @DisplayName("Deve lançar ReferenceException ao deletar Barbeiro com agendamentos")
    void onBeforeDeleteBarber_ShouldThrowException_WhenAppointmentsExist() {
        // Arrange
        BeforeDeleteBarber event = new BeforeDeleteBarber(testBarberId);
        Appointments mockAppointment = new Appointments(); // Um agendamento de mock
        mockAppointment.setId(1L);

        // Moca o repositório para retornar um agendamento
        when(appointmentsRepository.findFirstByBarberId(testBarberId)).thenReturn(mockAppointment);

        // Act & Assert
        // Verifica se a exceção é lançada
        ReferenceException exception = assertThrows(ReferenceException.class, () -> {
            entityDeletionListener.on(event);
        }, "Uma ReferenceException deveria ser lançada.");

        // Verifica a mensagem (chave) da exceção
        assertEquals("barber.appointments.barber.referenced", exception.getMessage());

        // Verifica se o repositório foi chamado
        verify(appointmentsRepository, times(1)).findFirstByBarberId(testBarberId);
    }

    @Test
    @DisplayName("Não deve lançar exceção ao deletar Barbeiro sem agendamentos")
    void onBeforeDeleteBarber_ShouldNotThrowException_WhenNoAppointmentsExist() {
        // Arrange
        BeforeDeleteBarber event = new BeforeDeleteBarber(testBarberId);

        // Moca o repositório para retornar nulo
        when(appointmentsRepository.findFirstByBarberId(testBarberId)).thenReturn(null);

        // Act & Assert
        // Verifica que nenhuma exceção é lançada
        assertDoesNotThrow(() -> {
            entityDeletionListener.on(event);
        }, "Nenhuma exceção deveria ser lançada.");

        // Verifica se o repositório foi chamado
        verify(appointmentsRepository, times(1)).findFirstByBarberId(testBarberId);
    }

    // --- Testes para BeforeDeleteCustomer ---

    @Test
    @DisplayName("Deve lançar ReferenceException ao deletar Cliente com agendamentos")
    void onBeforeDeleteCustomer_ShouldThrowException_WhenAppointmentsExist() {
        // Arrange
        BeforeDeleteCustomer event = new BeforeDeleteCustomer(testCustomerId);
        Appointments mockAppointment = new Appointments(); // Um agendamento de mock
        mockAppointment.setId(2L);

        // Moca o repositório para retornar um agendamento
        when(appointmentsRepository.findFirstByCustomerId(testCustomerId)).thenReturn(mockAppointment);

        // Act & Assert
        // Verifica se a exceção é lançada
        ReferenceException exception = assertThrows(ReferenceException.class, () -> {
            entityDeletionListener.on(event);
        }, "Uma ReferenceException deveria ser lançada.");

        // Verifica a mensagem (chave) da exceção
        assertEquals("customer.appointments.customer.referenced", exception.getMessage());

        // Verifica se o repositório foi chamado
        verify(appointmentsRepository, times(1)).findFirstByCustomerId(testCustomerId);
    }

    @Test
    @DisplayName("Não deve lançar exceção ao deletar Cliente sem agendamentos")
    void onBeforeDeleteCustomer_ShouldNotThrowException_WhenNoAppointmentsExist() {
        // Arrange
        BeforeDeleteCustomer event = new BeforeDeleteCustomer(testCustomerId);

        // Moca o repositório para retornar nulo
        when(appointmentsRepository.findFirstByCustomerId(testCustomerId)).thenReturn(null);

        // Act & Assert
        // Verifica que nenhuma exceção é lançada
        assertDoesNotThrow(() -> {
            entityDeletionListener.on(event);
        }, "Nenhuma exceção deveria ser lançada.");

        // Verifica se o repositório foi chamado
        verify(appointmentsRepository, times(1)).findFirstByCustomerId(testCustomerId);
    }
}