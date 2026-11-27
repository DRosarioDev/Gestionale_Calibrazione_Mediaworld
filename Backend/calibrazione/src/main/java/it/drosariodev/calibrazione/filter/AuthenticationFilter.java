package it.drosariodev.calibrazione.filter;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import it.drosariodev.calibrazione.service.CustomUserDetailsService;
import it.drosariodev.calibrazione.util.JWTUtil;


@Configuration
@EnableWebSecurity
public class AuthenticationFilter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private RestAccessDeniedHandler restAccessDeniedHandler;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
       
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }


    @SuppressWarnings("removal")
    @Bean
    public DefaultSecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/ping").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Frontend Angular servito dallo stesso backend: file statici e
                // rotte del client (es. /televisori/12) sono pubblici. Le regole
                // sottostanti continuano a proteggere tutto cio' che e' in /api.
                .requestMatchers(request -> !request.getServletPath().startsWith("/api")).permitAll()

                // Gestione utenti riservata agli amministratori.
                .requestMatchers("/api/utenti", "/api/utenti/**").hasRole("ADMIN")

                // L'eliminazione di una televisione è consentita agli utenti
                // autenticati; il service verifica che un tecnico possa
                // eliminare solo le televisioni inserite da lui stesso.
                .requestMatchers(HttpMethod.DELETE, "/api/televisori", "/api/televisori/**").authenticated()

                // Lettura e scrittura delle televisioni richiedono un utente autenticato.
                .requestMatchers("/api/televisori", "/api/televisori/**").authenticated()

                // Consultazione delle calibrazioni riservata agli utenti autenticati.
                .requestMatchers("/api/calibrazioni", "/api/calibrazioni/**").authenticated()

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler)
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(new JWTAuthenticationFilter(customUserDetailsService, jwtUtil),
                    UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

}
