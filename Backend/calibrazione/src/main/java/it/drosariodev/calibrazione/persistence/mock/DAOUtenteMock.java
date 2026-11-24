package it.drosariodev.calibrazione.persistence.mock;

import java.util.Locale;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;

@Repository 
@Profile ("mock")
public class DAOUtenteMock extends DAOGenericMock<Utente> implements IDAOUtente  {

    @Override
    public Utente findByEmail(String email) throws DAOException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String emailNormalizzata = email.trim().toLowerCase(Locale.ROOT);
        return findAll().stream()
                .filter(utente -> utente.getEmail() != null
                        && utente.getEmail().toLowerCase(Locale.ROOT).equals(emailNormalizzata))
                .findFirst()
                .orElse(null);
    }
    
}
