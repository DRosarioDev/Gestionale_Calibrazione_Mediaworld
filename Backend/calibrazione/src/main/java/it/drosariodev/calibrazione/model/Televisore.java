package it.drosariodev.calibrazione.model;

import java.time.LocalDateTime;

import it.drosariodev.calibrazione.enums.EMarca;
import it.drosariodev.calibrazione.enums.EStato;
import it.drosariodev.calibrazione.enums.ETecnologia;
import lombok.Data;

@Data
public class Televisore {

    private Long id;

    private EMarca marca;
    private String modello;
    private String codiceSeriale;
    private int pollici;
    private ETecnologia tecnologia;
    private EStato stato;
    private LocalDateTime dataAggiunta;
    private LocalDateTime dataCalibrazione;
    private String aggiuntoDa;
    private String note;
    private Calibrazione calibrazione;

}
