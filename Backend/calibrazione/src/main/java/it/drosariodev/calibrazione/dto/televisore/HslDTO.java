package it.drosariodev.calibrazione.dto.televisore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class HslDTO {

    @Min(0)
    @Max(360)
    private int h;

    @Min(0)
    @Max(100)
    private int s;

    @Min(0)
    @Max(100)
    private int l;

}
