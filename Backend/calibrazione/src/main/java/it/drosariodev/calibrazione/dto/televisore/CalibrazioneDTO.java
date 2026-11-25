package it.drosariodev.calibrazione.dto.televisore;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CalibrazioneDTO {

    private String id;
    private LocalDateTime dataCalibrazione;
    private String tecnico;
    private String note;
    /** Report .txt personalizzato salvato per questa calibrazione. */
    private String report;
    private ValoriCalibrazioneDTO valori;

}
