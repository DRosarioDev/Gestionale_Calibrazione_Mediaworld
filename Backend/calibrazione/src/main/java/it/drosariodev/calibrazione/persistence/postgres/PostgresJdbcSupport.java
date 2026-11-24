package it.drosariodev.calibrazione.persistence.postgres;

import java.sql.PreparedStatement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Piccole utilita' condivise dagli DAO PostgreSQL.
 */
final class PostgresJdbcSupport {

    private PostgresJdbcSupport() {
    }

    /**
     * Esegue un INSERT e restituisce la chiave generata.
     * Su PostgreSQL va indicata esplicitamente la colonna "id": chiedendo
     * tutte le chiavi il driver restituirebbe l'intera riga e
     * KeyHolder.getKey() fallirebbe.
     */
    static long insert(JdbcTemplate jdbcTemplate, String sql, Object... argomenti) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connessione -> {
            PreparedStatement statement = connessione.prepareStatement(sql, new String[] { "id" });
            for (int i = 0; i < argomenti.length; i++) {
                statement.setObject(i + 1, argomenti[i]);
            }
            return statement;
        }, keyHolder);

        Number chiave = keyHolder.getKey();
        if (chiave == null) {
            throw new IllegalStateException("Il database non ha restituito l'id generato.");
        }
        return chiave.longValue();
    }

    static <T extends Enum<T>> T valoreEnum(Class<T> tipo, String valore) {
        return valore == null ? null : Enum.valueOf(tipo, valore);
    }

    static String nomeEnum(Enum<?> valore) {
        return valore == null ? null : valore.name();
    }

}
