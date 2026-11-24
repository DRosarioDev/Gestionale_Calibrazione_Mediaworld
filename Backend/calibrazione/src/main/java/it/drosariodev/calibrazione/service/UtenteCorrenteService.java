package it.drosariodev.calibrazione.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;

/**
 * Recupera l'utente collegato al token della richiesta corrente.
 * I dati di sicurezza (aggiuntoDa, tecnico) non arrivano mai dal body.
 */
@Service
public class UtenteCorrenteService {

    @Autowired
    private IDAOUtente daoUtente;

    public Utente corrente() throws DAOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non autenticato.");
        }

        Utente utente = daoUtente.findByEmail(authentication.getName());
        if (utente == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente autenticato non trovato.");
        }
        return utente;
    }

    public String nomeCompleto(Utente utente) {
        return (utente.getNome() + " " + utente.getCognome()).trim();
    }

}
