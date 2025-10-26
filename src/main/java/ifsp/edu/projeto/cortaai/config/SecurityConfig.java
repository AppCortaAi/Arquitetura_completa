package ifsp.edu.projeto.cortaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // IMPORTAR ESTE
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("AdminCortaai@2112"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Permite acesso PÚBLICO aos endpoints de registro, login e UPLOAD
                        .requestMatchers(
                                "/api/customers/register",
                                "/api/customers/login",
                                "/api/barbers/register",
                                "/api/barbers/login"
                        ).permitAll()

                        // NOVAS REGRAS DE UPLOAD (permitAll para teste)
                        // Em produção, proteja isso com autenticação!
                        .requestMatchers(HttpMethod.POST,
                                "/api/customers/**/upload-photo",
                                "/api/barbers/**/upload-photo",
                                "/api/barbers/**/barbershops/upload-logo",
                                "/api/barbers/**/barbershops/upload-banner",
                                "/api/barbers/**/activities/**/upload-photo",
                                "/api/barbers/**/barbershops/highlights"
                        ).permitAll()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/barbers/**/barbershops/highlights/**"
                        ).permitAll()


                        // 2. Protege o Swagger e TODO O RESTANTE DA API com a role "ADMIN"
                        .requestMatchers(
                                "/", // Raiz (que redireciona pro Swagger)
                                "/swagger-ui/**", // Interface do Swagger
                                "/v3/api-docs/**",  // Definição da API (JSON)
                                "/api/**" // Todos os outros endpoints da API (que não deram match acima)
                        ).hasRole("ADMIN")

                        // 3. Qualquer outra requisição não mapeada
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());
        return http.build();
    }
}