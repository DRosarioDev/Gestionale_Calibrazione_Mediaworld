package it.drosariodev.calibrazione.dto.utente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CambiaPasswordRequest {

    @NotBlank(message = "Inserisci la password attuale.")
    private String passwordAttuale;

    @NotBlank(message = "Inserisci la nuova password.")
    @Size(min = 8, message = "La nuova password deve contenere almeno 8 caratteri.")
    private String nuovaPassword;

}
