package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Contenuto del report .txt personalizzato che il tecnico salva dopo la
 * calibrazione. Il testo e' libero: l'app permette di modificarlo prima del
 * download, quindi va conservato cosi' come e' stato scritto.
 */
@Data
public class SalvaReportRequest {

    @Size(max = 100000, message = "Il report è troppo lungo.")
    private String report;

}
