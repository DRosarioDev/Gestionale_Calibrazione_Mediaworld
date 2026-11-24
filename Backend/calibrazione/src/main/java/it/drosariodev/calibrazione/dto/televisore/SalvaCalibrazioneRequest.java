package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalvaCalibrazioneRequest {

    private String note;

    @NotNull
    @Valid
    private ValoriCalibrazioneDTO valori;

}
