package it.drosariodev.calibrazione.dto.utente;

import java.time.LocalDateTime;

import it.drosariodev.calibrazione.enums.ERuolo;
import lombok.Data;

@Data
public class UtenteDTO {

    private String id;
    private String nome;
    private String cognome;
    private String email;
    private ERuolo ruolo;
    private boolean attivo;
    private LocalDateTime dataCreazione;

}
