package ifsp.edu.projeto.cortaai.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para HomeController")
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController; // Classe sob teste

    @Test
    @DisplayName("Deve retornar a string de redirecionamento para o Swagger UI")
    void index_ShouldReturnSwaggerRedirectString() {
        // Arrange
        String expectedRedirectString = "redirect:/swagger-ui/index.html";

        // Act
        String actualResult = homeController.index();

        // Assert
        assertEquals(expectedRedirectString, actualResult, "O método index() deve retornar a string de redirecionamento exata.");
    }
}