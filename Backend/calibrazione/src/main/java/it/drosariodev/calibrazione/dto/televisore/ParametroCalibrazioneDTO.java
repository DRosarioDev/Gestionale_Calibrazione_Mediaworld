package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParametroCalibrazioneDTO {

    @NotBlank
    @Size(max = 80)
    private String etichetta;

    @Size(max = 120)
    private String valore;

}
