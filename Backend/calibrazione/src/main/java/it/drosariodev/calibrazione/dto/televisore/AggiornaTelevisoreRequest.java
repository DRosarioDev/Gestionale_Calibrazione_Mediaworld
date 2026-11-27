package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Dati anagrafici modificabili di una televisione.
 * Stato, date, autore e calibrazione non sono modificabili da qui.
 */
@Data
public class AggiornaTelevisoreRequest {

    @NotBlank
    private String marca;

    @NotBlank
    @Size(min = 2)
    private String modello;

    @Size(max = 100)
    private String codiceSeriale;

    @Min(10)
    @Max(150)
    private int pollici;

    @NotBlank
    private String tecnologia;

    private String note;

}
