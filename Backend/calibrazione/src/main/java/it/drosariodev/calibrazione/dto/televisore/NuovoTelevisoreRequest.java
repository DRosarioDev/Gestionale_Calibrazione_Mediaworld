package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NuovoTelevisoreRequest {

    @NotBlank
    private String marca;

    @NotBlank
    @Size(min = 2)
    private String modello;

    @NotBlank
    @Size(min = 4)
    private String codiceSeriale;

    @Min(10)
    @Max(150)
    private int pollici;

    @NotBlank
    private String tecnologia;

    private String note;

}
