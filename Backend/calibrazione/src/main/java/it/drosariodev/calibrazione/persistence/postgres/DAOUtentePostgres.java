package it.drosariodev.calibrazione.persistence.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.enums.ERuolo;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;

/**
 * Persistenza degli utenti su PostgreSQL (profilo "postgres").
 */
@Repository
@Profile("postgres")
public class DAOUtentePostgres implements IDAOUtente {

    private static final String SELECT_BASE = """
            SELECT id, nome, cognome, email, password, ruolo, attivo, data_creazione
            FROM utenti
            """;

    private static final String INSERT = """
            INSERT INTO utenti (nome, cognome, email, password, ruolo, attivo, data_creazione)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE utenti
               SET nome = ?, cognome = ?, email = ?, password = ?, ruolo = ?, attivo = ?, data_creazione = ?
             WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public DAOUtentePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Utente findById(Long id) throws DAOException {
        if (id == null) {
            return null;
        }
        try {
            List<Utente> risultati = jdbcTemplate.query(
                    SELECT_BASE + " WHERE id = ?", this::mappaUtente, id);
            return risultati.isEmpty() ? null : risultati.get(0);
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la lettura dell'utente con id " + id, ex);
        }
    }

    @Override
    public List<Utente> findAll() throws DAOException {
        try {
            return jdbcTemplate.query(SELECT_BASE + " ORDER BY id", this::mappaUtente);
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la lettura degli utenti", ex);
        }
    }

    @Override
    public Utente makePersistent(Utente utente) throws DAOException {
        try {
            if (utente.getId() == null) {
                utente.setId(PostgresJdbcSupport.insert(jdbcTemplate, INSERT,
                        utente.getNome(),
                        utente.getCognome(),
                        utente.getEmail(),
                        utente.getPassword(),
                        PostgresJdbcSupport.nomeEnum(utente.getRuolo()),
                        utente.isAttivo(),
                        utente.getDataCreazione()));
            } else {
                jdbcTemplate.update(UPDATE,
                        utente.getNome(),
                        utente.getCognome(),
                        utente.getEmail(),
                        utente.getPassword(),
                        PostgresJdbcSupport.nomeEnum(utente.getRuolo()),
                        utente.isAttivo(),
                        utente.getDataCreazione(),
                        utente.getId());
            }
            return utente;
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante il salvataggio dell'utente " + utente.getEmail(), ex);
        }
    }

    @Override
    public void makeTransient(Utente utente) throws DAOException {
        if (utente == null || utente.getId() == null) {
            return;
        }
        try {
            jdbcTemplate.update("DELETE FROM utenti WHERE id = ?", utente.getId());
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante l'eliminazione dell'utente " + utente.getId(), ex);
        }
    }

    @Override
    public Utente findByEmail(String email) throws DAOException {
        if (email == null || email.isBlank()) {
            return null;
        }
        try {
            List<Utente> risultati = jdbcTemplate.query(
                    SELECT_BASE + " WHERE LOWER(email) = LOWER(?)", this::mappaUtente, email.trim());
            return risultati.isEmpty() ? null : risultati.get(0);
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la ricerca dell'utente per email", ex);
        }
    }

    private Utente mappaUtente(ResultSet rs, int rowNum) throws SQLException {
        Utente utente = new Utente();
        utente.setId(rs.getLong("id"));
        utente.setNome(rs.getString("nome"));
        utente.setCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setPassword(rs.getString("password"));
        utente.setRuolo(PostgresJdbcSupport.valoreEnum(ERuolo.class, rs.getString("ruolo")));
        utente.setAttivo(rs.getBoolean("attivo"));
        utente.setDataCreazione(rs.getObject("data_creazione", LocalDateTime.class));
        return utente;
    }

}
