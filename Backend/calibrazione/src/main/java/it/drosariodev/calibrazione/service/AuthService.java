package it.drosariodev.calibrazione.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import it.drosariodev.calibrazione.dto.auth.LoginRequest;
import it.drosariodev.calibrazione.dto.auth.LoginResponse;
import it.drosariodev.calibrazione.dto.utente.CambiaPasswordRequest;
import it.drosariodev.calibrazione.dto.utente.UtenteDTO;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;
import it.drosariodev.calibrazione.util.JWTUtil;
import it.drosariodev.calibrazione.util.Mapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private IDAOUtente daoUtente;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UtenteCorrenteService utenteCorrenteService;

    @Autowired
    private JWTUtil jwtUtil;

    public LoginResponse login(LoginRequest request) throws DAOException {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Utente utente = daoUtente.findByEmail(email);

        if (utente == null || !passwordEncoder.matches(request.getPassword(), utente.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o password non corretti.");
        }

        if (!utente.isAttivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Profilo disattivato. Contatta un amministratore.");
        }

        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(utente.getEmail()));
        response.setUtente(Mapper.map(utente, UtenteDTO.class));

        log.info("Login effettuato da {} con ruolo {}", utente.getEmail(), utente.getRuolo());
        return response;
    }

    public UtenteDTO utenteCorrente() throws DAOException {
        return Mapper.map(utenteCorrenteService.corrente(), UtenteDTO.class);
    }

    public void cambiaPassword(CambiaPasswordRequest request) throws DAOException {
        Utente utente = utenteCorrenteService.corrente();

        if (!passwordEncoder.matches(request.getPasswordAttuale(), utente.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La password attuale non è corretta.");
        }

        if (passwordEncoder.matches(request.getNuovaPassword(), utente.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nuova password deve essere diversa da quella attuale.");
        }

        utente.setPassword(passwordEncoder.encode(request.getNuovaPassword()));
        daoUtente.makePersistent(utente);
        log.info("Password aggiornata dall'utente {}", utente.getEmail());
    }

}
