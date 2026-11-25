package it.drosariodev.calibrazione.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.drosariodev.calibrazione.dto.utente.NuovoUtenteRequest;
import it.drosariodev.calibrazione.dto.utente.ReimpostaPasswordRequest;
import it.drosariodev.calibrazione.dto.utente.UtenteDTO;
import it.drosariodev.calibrazione.enums.ERuolo;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;
import it.drosariodev.calibrazione.util.Mapper;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class UtenteService {

    @Autowired
    private IDAOUtente daoUtente;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UtenteDTO> findAll() throws DAOException {
        return Mapper.map(daoUtente.findAll(), UtenteDTO.class);
    }

    public UtenteDTO findById(Long id) throws DAOException {
        return Mapper.map(trovaUtente(id), UtenteDTO.class);
    }

    public Utente findByEmail(String email) throws DAOException {
        if (email == null || email.isBlank()) {
            return null;
        }
        return daoUtente.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    }

    public UtenteDTO crea(NuovoUtenteRequest request) throws DAOException {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (daoUtente.findByEmail(email) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esiste già un profilo con questa email.");
        }

        Utente utente = new Utente();
        utente.setNome(request.getNome().trim());
        utente.setCognome(request.getCognome().trim());
        utente.setEmail(email);
        utente.setRuolo(request.getRuolo());
        utente.setPassword(passwordEncoder.encode(request.getPassword()));
        utente.setAttivo(true);
        utente.setDataCreazione(LocalDateTime.now());

        daoUtente.makePersistent(utente);
        log.info("Creato utente {} {} con ruolo {}", utente.getNome(), utente.getCognome(), utente.getRuolo());

        return Mapper.map(utente, UtenteDTO.class);
    }

    public UtenteDTO aggiornaStato(Long id, boolean attivo) throws DAOException {
        Utente utente = trovaUtente(id);

        if (!attivo
                && utente.isAttivo()
                && utente.getRuolo() == ERuolo.ADMIN
                && !esistonoAltriAmministratoriAttivi(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Non è possibile disattivare l'ultimo amministratore attivo.");
        }

        utente.setAttivo(attivo);
        daoUtente.makePersistent(utente);
        log.info("Utente {} {} impostato a {}", utente.getNome(), utente.getCognome(), attivo ? "attivo" : "disattivo");

        return Mapper.map(utente, UtenteDTO.class);
    }

    public void reimpostaPassword(Long id, ReimpostaPasswordRequest request) throws DAOException {
        Utente utente = trovaUtente(id);

        if (passwordEncoder.matches(request.getNuovaPassword(), utente.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nuova password deve essere diversa da quella attuale.");
        }

        utente.setPassword(passwordEncoder.encode(request.getNuovaPassword()));
        daoUtente.makePersistent(utente);
        log.info("Password reimpostata dall'amministratore per {} {}", utente.getNome(), utente.getCognome());
    }

    private Utente trovaUtente(Long id) throws DAOException {
        Utente utente = daoUtente.findById(id);
        if (utente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utente non trovato.");
        }
        return utente;
    }

    private boolean esistonoAltriAmministratoriAttivi(Long id) throws DAOException {
        return daoUtente.findAll().stream()
                .filter(utente -> !Objects.equals(utente.getId(), id))
                .anyMatch(utente -> utente.getRuolo() == ERuolo.ADMIN && utente.isAttivo());
    }

}
