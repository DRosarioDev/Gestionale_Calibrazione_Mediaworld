package it.drosariodev.calibrazione.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Fallback per il routing di Angular.
 *
 * Il frontend compilato viene copiato in classpath:/static. Le sue rotte
 * (es. /televisori/12) non corrispondono a file reali: senza questo filtro
 * Spring risponderebbe 404 al refresh della pagina. Le richieste GET "da
 * pagina" ricevono quindi direttamente index.html e il router Angular fa il
 * resto. API, Swagger e file statici passano invariati.
 *
 * Il filtro si attiva solo se index.html e' presente nel pacchetto, quindi
 * non disturba l'avvio del solo backend in sviluppo.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SpaFallbackFilter extends OncePerRequestFilter {

    private static final Set<String> PREFISSI_RISERVATI = Set.of(
            "/api", "/swagger-ui", "/v3/api-docs", "/actuator");

    private final byte[] indexHtml = leggiIndex();

    private static byte[] leggiIndex() {
        try (InputStream in = SpaFallbackFilter.class.getResourceAsStream("/static/index.html")) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI().substring(request.getContextPath().length());

        boolean get = "GET".equalsIgnoreCase(request.getMethod());
        boolean riservato = PREFISSI_RISERVATI.stream().anyMatch(path::startsWith);
        boolean fileStatico = path.contains(".");

        if (indexHtml != null && get && !riservato && !fileStatico) {
            response.setContentType("text/html;charset=UTF-8");
            response.setContentLength(indexHtml.length);
            response.getOutputStream().write(indexHtml);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
