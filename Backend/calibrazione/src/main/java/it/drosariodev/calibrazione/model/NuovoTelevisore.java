package it.drosariodev.calibrazione.model;

import java.time.LocalDate;

import it.drosariodev.calibrazione.enums.EMarca;
import it.drosariodev.calibrazione.enums.EStato;
import it.drosariodev.calibrazione.enums.ETecnologia;
import lombok.Data;

@Data 
public class NuovoTelevisore {
    
    private Long id;
    private EMarca marca;
    private String modello;
    private String codiceSeriale;
    private int pollici;
    private ETecnologia tecnologia;
    private EStato stato;
    private LocalDate dataAggiunta;
    private String note;
    private Calibrazione calibrazione;

}
