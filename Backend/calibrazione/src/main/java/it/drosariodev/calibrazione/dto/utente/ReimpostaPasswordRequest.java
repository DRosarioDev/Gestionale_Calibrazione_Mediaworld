package it.drosariodev.calibrazione.dto.utente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReimpostaPasswordRequest {

    @NotBlank(message = "Inserisci la nuova password.")
    @Size(min = 8, message = "La nuova password deve contenere almeno 8 caratteri.")
    private String nuovaPassword;

}
