package it.drosariodev.calibrazione.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Calibrazione {

    private Long id;

    private LocalDateTime dataCalibrazione;
    private String tecnico;
    private String note;
    /** Report .txt personalizzato dal tecnico dopo la calibrazione. */
    private String report;
    private Valori valori;

}
