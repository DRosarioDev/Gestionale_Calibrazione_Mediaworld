package it.drosariodev.calibrazione.dto.televisore;

import java.time.LocalDateTime;

import it.drosariodev.calibrazione.enums.EStato;
import lombok.Data;

@Data
public class TelevisoreDTO {

    private String id;
    private String marca;
    private String modello;
    private String codiceSeriale;
    private int pollici;
    private String tecnologia;
    private EStato stato;
    private LocalDateTime dataAggiunta;
    private LocalDateTime dataCalibrazione;
    private String aggiuntoDa;
    private String note;
    private CalibrazioneDTO calibrazione;

}
