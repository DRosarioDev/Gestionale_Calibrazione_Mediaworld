package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ColoreCalibrazioneDTO {

    @NotNull
    @Valid
    private RgbDTO rgb;

    @NotNull
    @Valid
    private HslDTO hsl;

}
