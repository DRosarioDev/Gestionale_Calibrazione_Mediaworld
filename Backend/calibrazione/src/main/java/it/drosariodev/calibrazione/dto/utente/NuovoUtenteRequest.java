package it.drosariodev.calibrazione.dto.utente;

import it.drosariodev.calibrazione.enums.ERuolo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NuovoUtenteRequest {

    @NotBlank
    @Size(min = 2)
    private String nome;

    @NotBlank
    @Size(min = 2)
    private String cognome;

    @NotBlank
    @Email
    @Pattern(regexp = "(?i)^[\\w.+-]+@mediaworld\\.it$", message = "L'email deve appartenere al dominio @mediaworld.it")
    private String email;

    @NotNull
    private ERuolo ruolo;

    @NotBlank
    @Size(min = 8)
    private String password;

}
