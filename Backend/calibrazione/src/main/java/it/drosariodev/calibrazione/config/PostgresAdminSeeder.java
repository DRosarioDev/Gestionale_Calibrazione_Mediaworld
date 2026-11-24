package it.drosariodev.calibrazione.config;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.drosariodev.calibrazione.enums.ERuolo;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;
import lombok.extern.slf4j.Slf4j;

/**
 * Crea l'amministratore iniziale su PostgreSQL.
 *
 * Il database non puo' contenere password in chiaro: l'hash BCrypt viene
 * calcolato a runtime con il PasswordEncoder dell'applicazione, quindi lo
 * stesso utente puo' essere creato anche da un semplice INSERT SQL solo se
 * si conosce l'hash. Il seeder evita questo problema ed e' idempotente:
 * se l'email esiste gia' non fa nulla.
 *
 * Configurazione (application-postgres.properties):
 *   app.seed.admin.enabled  - true per creare l'utente al primo avvio
 *   app.seed.admin.email    - email dell'amministratore
 *   app.seed.admin.password - password iniziale, cifrata prima del salvataggio
 */
@Component
@Profile("postgres")
@ConditionalOnProperty(name = "app.seed.admin.enabled", havingValue = "true", matchIfMissing = true)
@Order(2)
@Slf4j
public class PostgresAdminSeeder implements ApplicationRunner {

    private final IDAOUtente daoUtente;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@mediaworld.it}")
    private String email;

    @Value("${app.seed.admin.password:Admin123!}")
    private String password;

    @Value("${app.seed.admin.nome:Marco}")
    private String nome;

    @Value("${app.seed.admin.cognome:Rossi}")
    private String cognome;

    public PostgresAdminSeeder(IDAOUtente daoUtente, PasswordEncoder passwordEncoder) {
        this.daoUtente = daoUtente;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws DAOException {
        String emailNormalizzata = email.trim().toLowerCase(Locale.ROOT);

        if (daoUtente.findByEmail(emailNormalizzata) != null) {
            log.debug("Amministratore iniziale {} gia' presente.", emailNormalizzata);
            return;
        }

        Utente amministratore = new Utente();
        amministratore.setNome(nome.trim());
        amministratore.setCognome(cognome.trim());
        amministratore.setEmail(emailNormalizzata);
        amministratore.setPassword(passwordEncoder.encode(password));
        amministratore.setRuolo(ERuolo.ADMIN);
        amministratore.setAttivo(true);
        amministratore.setDataCreazione(LocalDateTime.now());

        daoUtente.makePersistent(amministratore);

        log.warn("Creato amministratore iniziale {}. "
                + "Cambiare la password e impostare APP_SEED_ADMIN=false in produzione.",
                emailNormalizzata);
    }

}
