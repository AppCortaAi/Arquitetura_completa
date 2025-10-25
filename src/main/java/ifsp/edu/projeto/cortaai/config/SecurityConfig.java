package ifsp.edu.projeto.cortaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // IMPORTAR ESTE
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso não autenticado aos seguintes endpoints:
                        .requestMatchers(
                                // Endpoints da documentação (Swagger)
                                "/",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",

                                // Endpoints públicos da nossa API (criação e login)
                                "/api/customers/create",
                                "/api/customers/login",
                                "/api/barbers/create"

                        ).permitAll()

                        // Exige autenticação para qualquer outra requisição
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}