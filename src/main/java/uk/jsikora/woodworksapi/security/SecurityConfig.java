package uk.jsikora.woodworksapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Włącz CORS
            .authorizeHttpRequests(auth -> auth.requestMatchers("/", "/public", "/response", "/items", "/login","/logout", "/css/**", "/js/**").permitAll()
                                               .anyRequest().authenticated()
                                  )
            .oauth2Login(form -> {})
            .formLogin(form -> {})
            .logout(logout -> logout.logoutSuccessUrl("/public")
                                    .invalidateHttpSession(true)
                                    .clearAuthentication(true)
                                    .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:4200"); // Zezwól na Angular
        configuration.addAllowedMethod("*"); // Zezwól na wszystkie metody (GET, POST, itp.)
        configuration.addAllowedHeader("*"); // Zezwól na wszystkie nagłówki
        configuration.setAllowCredentials(true); // Jeśli używasz ciasteczek/autoryzacji

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Zastosuj do wszystkich endpointów
        return source;
    }
}