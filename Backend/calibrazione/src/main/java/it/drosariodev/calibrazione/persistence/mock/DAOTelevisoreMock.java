package it.drosariodev.calibrazione.persistence.mock;

import java.util.Locale;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.model.Televisore;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOTelevisore;

@Repository
@Profile("mock")
public class DAOTelevisoreMock extends DAOGenericMock<Televisore> implements IDAOTelevisore {

    @Override
    public Televisore findByCodiceSeriale(String codiceSeriale) throws DAOException {
        if (codiceSeriale == null || codiceSeriale.isBlank()) {
            return null;
        }
        String serialeNormalizzato = codiceSeriale.trim().toUpperCase(Locale.ROOT);
        return findAll().stream()
                .filter(televisore -> televisore.getCodiceSeriale() != null
                        && televisore.getCodiceSeriale().toUpperCase(Locale.ROOT).equals(serialeNormalizzato))
                .findFirst()
                .orElse(null);
    }

}
