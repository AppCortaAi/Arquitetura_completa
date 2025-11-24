package ifsp.edu.projeto.cortaai.config;

import com.cloudinary.Cloudinary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração para a classe CloudinaryConfig.
 *
 * Esta classe verifica se o contexto do Spring é carregado corretamente
 * e se o bean do Cloudinary é criado e configurado a partir das
 * propriedades do ambiente de teste.
 */
@SpringBootTest(classes = CloudinaryConfig.class) // 1. Carrega apenas a classe de configuração alvo
@TestPropertySource(properties = {
        "CLOUDINARY_URL=cloudinary://192355578827798:psQi0OqTbahg46XXJAvJMXktawQ@dsnig53xw" // 2. Fornece uma URL mock para o teste
})
class CloudinaryConfigIntegrationTest {

    // 3. Injeta o bean criado pelo Spring a partir da nossa configuração
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load Spring context and create Cloudinary bean")
    void contextLoadsAndCloudinaryBeanIsCreated() {
        // Verifica se o bean existe no contexto do Spring
        assertNotNull(applicationContext.getBean(Cloudinary.class), "O bean do Cloudinary não deveria ser nulo.");
        // Verifica se o bean injetado não é nulo
        assertNotNull(cloudinary, "A instância injetada do Cloudinary não deveria ser nula.");
    }

    @Test
    @DisplayName("Should configure Cloudinary bean with properties from test environment")
    void cloudinaryBeanIsConfiguredCorrectly() {
        // As asserções abaixo garantem que a URL de teste foi corretamente
        // parseada pelo SDK do Cloudinary ao criar o bean.

        // Extrai a configuração do bean injetado
        var config = cloudinary.config;

        // Valida se os valores correspondem aos da URL de teste
        assertEquals("cloudinary//192355578827798:psQi0OqTbahg46XXJAvJMXktawQ@dsnig53xw", config.cloudName, "O 'cloud_name' deveria ser o mesmo da URL de teste.");
        assertEquals("meu_api_key", config.apiKey, "A 'api_key' deveria ser a mesma da URL de teste.");
        assertEquals("minha_api_secret", config.apiSecret, "A 'api_secret' deveria ser a mesma da URL de teste.");
    }
}