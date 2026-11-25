package it.drosariodev.calibrazione.model;

import lombok.Data;

/**
 * I tre valori che il tecnico inserisce nella TV per un singolo colore RGBCMY.
 * Non esiste alcuna conversione: sono esattamente i numeri letti/scritti nel
 * menu della televisione (l'ordine e il significato dipendono dal modello).
 */
@Data
public class ColoreCalibrazione {

    private int valore1;
    private int valore2;
    private int valore3;

}
