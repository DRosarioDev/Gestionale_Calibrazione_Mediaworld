package it.drosariodev.calibrazione.dto.utente;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AggiornaStatoUtenteRequest {

    @NotNull
    private Boolean attivo;

}
