package it.drosariodev.calibrazione.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariDataSource;

/**
 * DataSource del profilo "postgres".
 *
 * Accetta DB_URL in tutti i formati usati dai servizi di hosting:
 *   - JDBC:          jdbc:postgresql://host:5432/db?sslmode=require
 *   - Render/Heroku: postgresql://utente:password@host:5432/db
 *   - Neon/Supabase: postgres://utente:password@host/db?sslmode=require
 *
 * In questo modo basta incollare la stringa di connessione fornita dal
 * provider (per esempio la variabile generata da render.yaml) senza doverla
 * riscrivere a mano. Se DB_URL non e' impostata si usano DB_HOST/DB_PORT/
 * DB_NAME/DB_USER/DB_PASSWORD, con gli stessi default dello sviluppo locale.
 *
 * Nota: il bean esiste solo con il profilo "postgres"; il profilo "mock"
 * continua a girare in memoria e non tocca il database.
 */
@Configuration
@Profile("postgres")
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        String raw = env.getProperty("DB_URL", "").trim();

        String url;
        String username;
        String password;

        if (raw.startsWith("jdbc:postgresql://")) {
            url = raw;
            username = env.getProperty("DB_USER", env.getProperty("spring.datasource.username", "calibrazione"));
            password = env.getProperty("DB_PASSWORD", env.getProperty("spring.datasource.password", "calibrazione"));
        } else if (raw.startsWith("postgresql://") || raw.startsWith("postgres://")) {
            URI uri = URI.create(raw);
            int porta = uri.getPort() == -1 ? 5432 : uri.getPort();
            String query = uri.getRawQuery();
            url = "jdbc:postgresql://" + uri.getHost() + ":" + porta + uri.getRawPath()
                    + (query == null || query.isBlank() ? "" : "?" + query);

            String userInfo = uri.getRawUserInfo();
            String[] credenziali = userInfo == null ? new String[0] : userInfo.split(":", 2);
            username = credenziali.length > 0
                    ? URLDecoder.decode(credenziali[0], StandardCharsets.UTF_8)
                    : env.getProperty("DB_USER", "calibrazione");
            password = credenziali.length > 1
                    ? URLDecoder.decode(credenziali[1], StandardCharsets.UTF_8)
                    : env.getProperty("DB_PASSWORD", "calibrazione");
        } else {
            url = "jdbc:postgresql://"
                    + env.getProperty("DB_HOST", "localhost") + ":"
                    + env.getProperty("DB_PORT", "5432") + "/"
                    + env.getProperty("DB_NAME", "calibrazione");
            username = env.getProperty("DB_USER", "calibrazione");
            password = env.getProperty("DB_PASSWORD", "calibrazione");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName(env.getProperty("spring.datasource.hikari.pool-name", "calibrazione-pool"));
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(Integer.parseInt(env.getProperty("DB_POOL_SIZE", "10")));
        dataSource.setMinimumIdle(Integer.parseInt(env.getProperty("DB_POOL_MIN_IDLE", "2")));
        dataSource.setConnectionTimeout(5000);
        dataSource.setValidationTimeout(3000);
        return dataSource;
    }
}
