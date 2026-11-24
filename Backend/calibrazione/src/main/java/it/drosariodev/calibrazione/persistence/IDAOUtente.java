package it.drosariodev.calibrazione.persistence;

import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.model.Utente;

@Repository 
public interface IDAOUtente extends IDAOGeneric<Utente>{
    
    Utente findByEmail(String email) throws DAOException;

}
