package it.drosariodev.calibrazione.dto.televisore;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CalibrazioneDTO {

    private String id;
    private LocalDateTime dataCalibrazione;
    private String tecnico;
    private String note;
    private ValoriCalibrazioneDTO valori;

}
