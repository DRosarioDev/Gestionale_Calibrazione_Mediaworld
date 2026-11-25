package it.drosariodev.calibrazione.model;

import java.util.List;

import lombok.Data;

@Data
public class Valori {

    private List<Parametro> parametri;
    private Rgb biancoGain;
    private Rgb biancoOffset;
    private boolean coloriAbilitati;
    private Colori colori;

}
