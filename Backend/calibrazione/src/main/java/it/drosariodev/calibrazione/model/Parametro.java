package it.drosariodev.calibrazione.model;

import lombok.Data;

/**
 * Parametro libero della calibrazione: ogni televisione espone voci diverse,
 * quindi il nome e il valore non sono vincolati a un elenco fisso.
 */
@Data
public class Parametro {

    private String etichetta;
    private String valore;

}
