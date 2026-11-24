package it.drosariodev.calibrazione.model;

import java.time.LocalDateTime;

import it.drosariodev.calibrazione.enums.ERuolo;
import lombok.Data;

@Data 
public class Utente{

    private Long id;

    private String nome;
    private String cognome;
    private String email;
    private String password;
    private ERuolo ruolo;
    private boolean isAttivo;
    private LocalDateTime dataCreazione;


}
