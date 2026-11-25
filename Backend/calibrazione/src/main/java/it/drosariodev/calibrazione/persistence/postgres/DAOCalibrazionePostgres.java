package it.drosariodev.calibrazione.persistence.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import it.drosariodev.calibrazione.model.Calibrazione;
import it.drosariodev.calibrazione.model.ColoreCalibrazione;
import it.drosariodev.calibrazione.model.Colori;
import it.drosariodev.calibrazione.model.Parametro;
import it.drosariodev.calibrazione.model.Rgb;
import it.drosariodev.calibrazione.model.Valori;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOCalibrazione;

/**
 * Persistenza delle calibrazioni su PostgreSQL (profilo "postgres").
 *
 * I valori annidati del model Valori sono normalizzati in tre tabelle:
 *   * calibrazione_valori    (1-a-1, bianco/offset/modalita')
 *   * calibrazione_parametri (N, con l'ordine originale)
 *   * calibrazione_colori    (fino a 6 righe, chiave in minuscolo)
 */
@Repository
@Profile("postgres")
public class DAOCalibrazionePostgres implements IDAOCalibrazione {

    private static final String SELECT_BASE = """
            SELECT id, data_calibrazione, tecnico, note, report
            FROM calibrazioni
            """;

    private static final String INSERT = """
            INSERT INTO calibrazioni (data_calibrazione, tecnico, note, report)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE = """
            UPDATE calibrazioni
               SET data_calibrazione = ?, tecnico = ?, note = ?, report = ?
             WHERE id = ?
            """;

    private static final String UPSERT_VALORI = """
            INSERT INTO calibrazione_valori (calibrazione_id,
                                             bianco_gain_r, bianco_gain_g, bianco_gain_b,
                                             bianco_offset_r, bianco_offset_g, bianco_offset_b,
                                             colori_abilitati)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (calibrazione_id) DO UPDATE
               SET bianco_gain_r = EXCLUDED.bianco_gain_r,
                   bianco_gain_g = EXCLUDED.bianco_gain_g,
                   bianco_gain_b = EXCLUDED.bianco_gain_b,
                   bianco_offset_r = EXCLUDED.bianco_offset_r,
                   bianco_offset_g = EXCLUDED.bianco_offset_g,
                   bianco_offset_b = EXCLUDED.bianco_offset_b,
                   colori_abilitati = EXCLUDED.colori_abilitati
            """;

    private static final String SELECT_VALORI = """
            SELECT bianco_gain_r, bianco_gain_g, bianco_gain_b,
                   bianco_offset_r, bianco_offset_g, bianco_offset_b,
                   colori_abilitati
            FROM calibrazione_valori
            WHERE calibrazione_id = ?
            """;

    private static final String INSERT_PARAMETRO = """
            INSERT INTO calibrazione_parametri (calibrazione_id, ordine, etichetta, valore)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_PARAMETRI = """
            SELECT etichetta, valore
            FROM calibrazione_parametri
            WHERE calibrazione_id = ?
            ORDER BY ordine, id
            """;

    private static final String INSERT_COLORE = """
            INSERT INTO calibrazione_colori (calibrazione_id, chiave,
                                             valore1, valore2, valore3)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SELECT_COLORI = """
            SELECT chiave, valore1, valore2, valore3
            FROM calibrazione_colori
            WHERE calibrazione_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public DAOCalibrazionePostgres(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Calibrazione findById(Long id) throws DAOException {
        if (id == null) {
            return null;
        }
        try {
            List<Calibrazione> risultati = jdbcTemplate.query(
                    SELECT_BASE + " WHERE id = ?", this::mappaCalibrazione, id);
            if (risultati.isEmpty()) {
                return null;
            }
            Calibrazione calibrazione = risultati.get(0);
            calibrazione.setValori(caricaValori(id));
            return calibrazione;
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la lettura della calibrazione con id " + id, ex);
        }
    }

    @Override
    public List<Calibrazione> findAll() throws DAOException {
        try {
            List<Calibrazione> calibrazioni = jdbcTemplate.query(
                    SELECT_BASE + " ORDER BY data_calibrazione DESC NULLS LAST, id DESC",
                    this::mappaCalibrazione);
            for (Calibrazione calibrazione : calibrazioni) {
                calibrazione.setValori(caricaValori(calibrazione.getId()));
            }
            return calibrazioni;
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante la lettura delle calibrazioni", ex);
        }
    }

    /**
     * Salva la calibrazione e sostituisce in blocco parametri e colori:
     * in questo modo i valori rimossi dal form non restano nel database.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Calibrazione makePersistent(Calibrazione calibrazione) throws DAOException {
        try {
            if (calibrazione.getId() == null) {
                calibrazione.setId(PostgresJdbcSupport.insert(jdbcTemplate, INSERT,
                        calibrazione.getDataCalibrazione(),
                        calibrazione.getTecnico(),
                        calibrazione.getNote(),
                        calibrazione.getReport()));
            } else {
                int aggiornate = jdbcTemplate.update(UPDATE,
                        calibrazione.getDataCalibrazione(),
                        calibrazione.getTecnico(),
                        calibrazione.getNote(),
                        calibrazione.getReport(),
                        calibrazione.getId());
                if (aggiornate == 0) {
                    throw new DAOException("Calibrazione con id " + calibrazione.getId() + " non trovata.");
                }
            }

            salvaValori(calibrazione);
            return calibrazione;
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante il salvataggio della calibrazione", ex);
        }
    }

    @Override
    public void makeTransient(Calibrazione calibrazione) throws DAOException {
        if (calibrazione == null || calibrazione.getId() == null) {
            return;
        }
        try {
            // Valori, parametri e colori vengono rimossi dal database tramite
            // ON DELETE CASCADE.
            jdbcTemplate.update("DELETE FROM calibrazioni WHERE id = ?", calibrazione.getId());
        } catch (DataAccessException ex) {
            throw new DAOException("Errore durante l'eliminazione della calibrazione "
                    + calibrazione.getId(), ex);
        }
    }

    private void salvaValori(Calibrazione calibrazione) {
        Long calibrazioneId = calibrazione.getId();

        jdbcTemplate.update("DELETE FROM calibrazione_parametri WHERE calibrazione_id = ?", calibrazioneId);
        jdbcTemplate.update("DELETE FROM calibrazione_colori WHERE calibrazione_id = ?", calibrazioneId);

        Valori valori = calibrazione.getValori();
        if (valori == null) {
            jdbcTemplate.update("DELETE FROM calibrazione_valori WHERE calibrazione_id = ?", calibrazioneId);
            return;
        }

        Rgb gain = valori.getBiancoGain() == null ? new Rgb() : valori.getBiancoGain();
        Rgb offset = valori.getBiancoOffset() == null ? new Rgb() : valori.getBiancoOffset();

        jdbcTemplate.update(UPSERT_VALORI,
                calibrazioneId,
                gain.getR(), gain.getG(), gain.getB(),
                offset.getR(), offset.getG(), offset.getB(),
                valori.isColoriAbilitati());

        salvaParametri(calibrazioneId, valori.getParametri());
        salvaColori(calibrazioneId, valori.getColori());
    }

    private void salvaParametri(Long calibrazioneId, List<Parametro> parametri) {
        if (parametri == null || parametri.isEmpty()) {
            return;
        }
        List<Object[]> righe = new ArrayList<>(parametri.size());
        int ordine = 0;
        for (Parametro parametro : parametri) {
            if (parametro == null) {
                continue;
            }
            righe.add(new Object[] {
                    calibrazioneId,
                    ordine++,
                    parametro.getEtichetta() == null ? "" : parametro.getEtichetta(),
                    parametro.getValore()
            });
        }
        if (!righe.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_PARAMETRO, righe);
        }
    }

    private void salvaColori(Long calibrazioneId, Colori colori) {
        for (Map.Entry<String, ColoreCalibrazione> voce : mappaColori(colori).entrySet()) {
            ColoreCalibrazione colore = voce.getValue();

            jdbcTemplate.update(INSERT_COLORE,
                    calibrazioneId,
                    voce.getKey(),
                    colore.getValore1(),
                    colore.getValore2(),
                    colore.getValore3());
        }
    }

    private Valori caricaValori(Long calibrazioneId) {
        List<Valori> risultati = jdbcTemplate.query(
                SELECT_VALORI, this::mappaValori, calibrazioneId);
        if (risultati.isEmpty()) {
            return null;
        }

        Valori valori = risultati.get(0);
        valori.setParametri(caricaParametri(calibrazioneId));
        valori.setColori(caricaColori(calibrazioneId));
        return valori;
    }

    private List<Parametro> caricaParametri(Long calibrazioneId) {
        return jdbcTemplate.query(SELECT_PARAMETRI, (rs, rowNum) -> {
            Parametro parametro = new Parametro();
            parametro.setEtichetta(rs.getString("etichetta"));
            parametro.setValore(rs.getString("valore"));
            return parametro;
        }, calibrazioneId);
    }

    private Colori caricaColori(Long calibrazioneId) {
        Map<String, ColoreCalibrazione> mappa = new HashMap<>();

        RowCallbackHandler lettoreColore = rs -> {
            ColoreCalibrazione colore = new ColoreCalibrazione();
            colore.setValore1(rs.getInt("valore1"));
            colore.setValore2(rs.getInt("valore2"));
            colore.setValore3(rs.getInt("valore3"));

            mappa.put(rs.getString("chiave"), colore);
        };

        jdbcTemplate.query(SELECT_COLORI, lettoreColore, calibrazioneId);
        if (mappa.isEmpty()) {
            return null;
        }

        Colori colori = new Colori();
        colori.setRosso(mappa.get("rosso"));
        colori.setVerde(mappa.get("verde"));
        colori.setBlu(mappa.get("blu"));
        colori.setCiano(mappa.get("ciano"));
        colori.setMagenta(mappa.get("magenta"));
        colori.setGiallo(mappa.get("giallo"));
        return colori;
    }

    private Map<String, ColoreCalibrazione> mappaColori(Colori colori) {
        Map<String, ColoreCalibrazione> mappa = new LinkedHashMap<>();
        if (colori == null) {
            return mappa;
        }
        mappa.put("rosso", colori.getRosso());
        mappa.put("verde", colori.getVerde());
        mappa.put("blu", colori.getBlu());
        mappa.put("ciano", colori.getCiano());
        mappa.put("magenta", colori.getMagenta());
        mappa.put("giallo", colori.getGiallo());
        mappa.values().removeIf(Objects::isNull);
        return mappa;
    }

    private Calibrazione mappaCalibrazione(ResultSet rs, int rowNum) throws SQLException {
        Calibrazione calibrazione = new Calibrazione();
        calibrazione.setId(rs.getLong("id"));
        calibrazione.setDataCalibrazione(rs.getObject("data_calibrazione", LocalDateTime.class));
        calibrazione.setTecnico(rs.getString("tecnico"));
        calibrazione.setNote(rs.getString("note"));
        calibrazione.setReport(rs.getString("report"));
        return calibrazione;
    }

    private Valori mappaValori(ResultSet rs, int rowNum) throws SQLException {
        Valori valori = new Valori();
        valori.setBiancoGain(mappaLetturaRgb(rs, "bianco_gain_"));
        valori.setBiancoOffset(mappaLetturaRgb(rs, "bianco_offset_"));
        valori.setColoriAbilitati(rs.getBoolean("colori_abilitati"));
        return valori;
    }

    private Rgb mappaLetturaRgb(ResultSet rs, String prefisso) throws SQLException {
        Rgb rgb = new Rgb();
        rgb.setR(rs.getInt(prefisso + "r"));
        rgb.setG(rs.getInt(prefisso + "g"));
        rgb.setB(rs.getInt(prefisso + "b"));
        return rgb;
    }

}
