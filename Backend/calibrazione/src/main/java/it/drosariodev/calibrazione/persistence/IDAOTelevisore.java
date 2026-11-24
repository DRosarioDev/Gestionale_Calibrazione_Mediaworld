package it.drosariodev.calibrazione.persistence;

import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.model.Televisore;

@Repository
public interface IDAOTelevisore extends IDAOGeneric<Televisore> {

    Televisore findByCodiceSeriale(String codiceSeriale) throws DAOException;

}
