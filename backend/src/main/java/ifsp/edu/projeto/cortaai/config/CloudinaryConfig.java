package ifsp.edu.projeto.cortaai.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    // Lê a URL completa do arquivo .env
    @Value("${CLOUDINARY_URL}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        // O SDK do Cloudinary é inteligente e sabe como
        // parsear a string de URL para se configurar.
        return new Cloudinary(cloudinaryUrl);
    }
}