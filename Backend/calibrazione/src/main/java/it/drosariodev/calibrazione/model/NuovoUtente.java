package it.drosariodev.calibrazione.model;

import it.drosariodev.calibrazione.enums.ERuolo;
import lombok.Data;

@Data
public class NuovoUtente {

    private String nome;
    private String cognome;
    private String email;
    private ERuolo ruolo;
    private String password;

}
