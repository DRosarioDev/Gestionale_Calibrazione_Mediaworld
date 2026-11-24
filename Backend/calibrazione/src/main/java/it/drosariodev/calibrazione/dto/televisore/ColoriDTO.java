package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColoriDTO {

    @NotNull
    @Valid
    private ColoreCalibrazioneDTO rosso;

    @NotNull
    @Valid
    private ColoreCalibrazioneDTO verde;

    @NotNull
    @Valid
    private ColoreCalibrazioneDTO blu;

    @NotNull
    @Valid
    private ColoreCalibrazioneDTO ciano;

    @NotNull
    @Valid
    private ColoreCalibrazioneDTO magenta;

    @NotNull
    @Valid
    private ColoreCalibrazioneDTO giallo;

}
