package it.drosariodev.calibrazione.dto.televisore;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValoriCalibrazioneDTO {

    @NotNull
    private List<@Valid ParametroCalibrazioneDTO> parametri;

    @NotNull
    @Valid
    private RgbDTO biancoGain;

    @NotNull
    @Valid
    private RgbDTO biancoOffset;

    private boolean coloriAbilitati;

    @NotNull
    @Valid
    private ColoriDTO colori;

}
