package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColoreCalibrazioneDTO {

    @NotNull
    private Integer valore1;

    @NotNull
    private Integer valore2;

    @NotNull
    private Integer valore3;

}
