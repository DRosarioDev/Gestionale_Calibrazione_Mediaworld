package it.drosariodev.calibrazione.persistence.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.enums.EMarca;
import it.drosariodev.calibrazione.enums.EStato;
import it.drosariodev.calibrazione.enums.ETecnologia;
import it.drosariodev.calibrazione.model.Calibrazione;
import it.drosariodev.calibrazione.model.Televisore;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOCalibrazione;
import it.drosariodev.calibrazione.persistence.IDAOTelevisore;

/**
 * Persistenza delle televisioni su PostgreSQL (profilo "postgres").
 */
@Repository
@Profile("postgres")
public class DAOTelevisorePostgres implements IDAOTelevisore {

    private static final String SELECT_BASE = """
            SELECT id, marca, modello, codice_seriale, pollici, tecnologia, stato,
                   data_aggiunta, data_calibrazione, aggiunto_da, note, calibrazione_id
            FROM televisori
            """;

    private static final String INSERT = """
            INSERT INTO televisori (marca, modello, codice_seriale, pollici, tecnologia, stato,
                                    data_aggiunta, data_calibrazione, aggiunto_da, note, calibrazione_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE televisori
               SET marca = ?, modello = ?, codice_seriale = ?, pollici = ?, tecnologia = ?, stato = ?,
                   data_aggiunta = ?, data_calibrazione = ?, aggiunto_da = ?, note = ?, calibrazione_id = ?
             WHERE id = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final IDAOCalibrazione daoCalibrazione;

    public DAOTelevisorePostgres(JdbcTemplate jdbcTemplate, IDAOCalibrazione daoCalibrazione) {
        this.jdbcTemplate = jdbcTemplate;
        this.daoCalibrazione = daoCalibrazione;
    }

    @Override
    public Televisore findById(Long id) throws DAOException {
        if (id == null) {
            return null;
        }
        try {
            List<Televisore> risultati = jdbcTemplate.query(
                    SELECT_BASE + " WHERE id = ?", this::mappaTelevisore, id);
            if (risultati.isEmpty()) {
                return null;
            }
            return caricaCalibrazione(risultati.get(0));
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la lettura della televisione con id " + id, ex);
        }
    }

    @Override
    public List<Televisore> findAll() throws DAOException {
        try {
            List<Televisore> televisori = jdbcTemplate.query(
                    SELECT_BASE + " ORDER BY data_aggiunta DESC NULLS LAST, id DESC",
                    this::mappaTelevisore);
            return caricaCalibrazioni(televisori);
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la lettura delle televisioni", ex);
        }
    }

    @Override
    public Televisore makePersistent(Televisore televisore) throws DAOException {
        Long calibrazioneId = televisore.getCalibrazione() == null
                ? null
                : televisore.getCalibrazione().getId();

        try {
            if (televisore.getId() == null) {
                televisore.setId(PostgresJdbcSupport.insert(jdbcTemplate, INSERT,
                        PostgresJdbcSupport.nomeEnum(televisore.getMarca()),
                        televisore.getModello(),
                        televisore.getCodiceSeriale(),
                        televisore.getPollici(),
                        PostgresJdbcSupport.nomeEnum(televisore.getTecnologia()),
                        PostgresJdbcSupport.nomeEnum(televisore.getStato()),
                        televisore.getDataAggiunta(),
                        televisore.getDataCalibrazione(),
                        televisore.getAggiuntoDa(),
                        televisore.getNote(),
                        calibrazioneId));
            } else {
                jdbcTemplate.update(UPDATE,
                        PostgresJdbcSupport.nomeEnum(televisore.getMarca()),
                        televisore.getModello(),
                        televisore.getCodiceSeriale(),
                        televisore.getPollici(),
                        PostgresJdbcSupport.nomeEnum(televisore.getTecnologia()),
                        PostgresJdbcSupport.nomeEnum(televisore.getStato()),
                        televisore.getDataAggiunta(),
                        televisore.getDataCalibrazione(),
                        televisore.getAggiuntoDa(),
                        televisore.getNote(),
                        calibrazioneId,
                        televisore.getId());
            }
            return televisore;
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante il salvataggio della televisione "
                    + televisore.getCodiceSeriale(), ex);
        }
    }

    @Override
    public void makeTransient(Televisore televisore) throws DAOException {
        if (televisore == null || televisore.getId() == null) {
            return;
        }
        try {
            jdbcTemplate.update("DELETE FROM televisori WHERE id = ?", televisore.getId());
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante l'eliminazione della televisione " + televisore.getId(), ex);
        }
    }

    @Override
    public Televisore findByCodiceSeriale(String codiceSeriale) throws DAOException {
        if (codiceSeriale == null || codiceSeriale.isBlank()) {
            return null;
        }
        try {
            List<Televisore> risultati = jdbcTemplate.query(
                    SELECT_BASE + " WHERE UPPER(codice_seriale) = UPPER(?)",
                    this::mappaTelevisore, codiceSeriale.trim());
            if (risultati.isEmpty()) {
                return null;
            }
            return caricaCalibrazione(risultati.get(0));
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la ricerca della televisione per codice seriale", ex);
        }
    }

    /**
     * Il row mapper valorizza solo l'id della calibrazione: il caricamento
     * completo avviene dopo, cosi' l'eventuale errore resta un DAOException.
     */
    private Televisore mappaTelevisore(ResultSet rs, int rowNum) throws SQLException {
        Televisore televisore = new Televisore();
        televisore.setId(rs.getLong("id"));
        televisore.setMarca(PostgresJdbcSupport.valoreEnum(EMarca.class, rs.getString("marca")));
        televisore.setModello(rs.getString("modello"));
        televisore.setCodiceSeriale(rs.getString("codice_seriale"));
        televisore.setPollici(rs.getInt("pollici"));
        televisore.setTecnologia(PostgresJdbcSupport.valoreEnum(ETecnologia.class, rs.getString("tecnologia")));
        televisore.setStato(PostgresJdbcSupport.valoreEnum(EStato.class, rs.getString("stato")));
        televisore.setDataAggiunta(rs.getObject("data_aggiunta", LocalDateTime.class));
        televisore.setDataCalibrazione(rs.getObject("data_calibrazione", LocalDateTime.class));
        televisore.setAggiuntoDa(rs.getString("aggiunto_da"));
        televisore.setNote(rs.getString("note"));

        long calibrazioneId = rs.getLong("calibrazione_id");
        if (!rs.wasNull()) {
            Calibrazione riferimento = new Calibrazione();
            riferimento.setId(calibrazioneId);
            televisore.setCalibrazione(riferimento);
        }
        return televisore;
    }

    private List<Televisore> caricaCalibrazioni(List<Televisore> televisori) throws DAOException {
        for (Televisore televisore : televisori) {
            caricaCalibrazione(televisore);
        }
        return televisori;
    }

    private Televisore caricaCalibrazione(Televisore televisore) throws DAOException {
        if (televisore.getCalibrazione() != null) {
            televisore.setCalibrazione(daoCalibrazione.findById(televisore.getCalibrazione().getId()));
        }
        return televisore;
    }

}
