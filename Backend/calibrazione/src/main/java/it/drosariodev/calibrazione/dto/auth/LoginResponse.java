package it.drosariodev.calibrazione.dto.auth;

import it.drosariodev.calibrazione.dto.utente.UtenteDTO;
import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private UtenteDTO utente;

}
