package it.drosariodev.calibrazione.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.drosariodev.calibrazione.enums.EMarca;
import it.drosariodev.calibrazione.enums.EModalitaColore;
import it.drosariodev.calibrazione.enums.ERuolo;
import it.drosariodev.calibrazione.enums.EStato;
import it.drosariodev.calibrazione.enums.ETecnologia;
import it.drosariodev.calibrazione.model.Calibrazione;
import it.drosariodev.calibrazione.model.ColoreCalibrazione;
import it.drosariodev.calibrazione.model.Colori;
import it.drosariodev.calibrazione.model.Hsl;
import it.drosariodev.calibrazione.model.Parametro;
import it.drosariodev.calibrazione.model.Rgb;
import it.drosariodev.calibrazione.model.Televisore;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.model.Valori;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOCalibrazione;
import it.drosariodev.calibrazione.persistence.IDAOTelevisore;
import it.drosariodev.calibrazione.persistence.IDAOUtente;
import lombok.extern.slf4j.Slf4j;

/**
 * Popola il repository con gli stessi dati demo usati dal frontend.
 * Con il profilo "mock" gira su RepositoryMock: i dati non vengono salvati
 * su database e si ricreano a ogni riavvio dell'applicazione.
 * Con il profilo "postgres" scrive sul database reale, ma solo se
 * app.seed.demo.enabled=true (disattivato di default).
 *
 * Gli store vengono toccati solo se sono vuoti: se un domani il repository
 * diventasse persistente il seeder non sovrascriverebbe i dati esistenti.
 */
@Component
@Profile({ "mock", "postgres" })
@Order(1)
@Slf4j
public class DatiDemoSeeder implements ApplicationRunner {

    private static final String[] NOTE_CALIBRAZIONE = {
        "Calibrazione eseguita in ambiente controllato con sonda colorimetrica. "
                + "Verificata su pattern SDR e HDR.",
        "Pannello uniformato dopo 30 minuti di riscaldamento. Bianco bilanciato su D65.",
        "Taratura ottimizzata per la visione in ambiente domestico con luce controllata. "
                + "Controllati clipping e banding.",
        "Calibrazione completata e validata con pattern di riferimento. "
                + "Gamma verificata su scala dei grigi.",
    };

    @Autowired
    private IDAOUtente daoUtente;

    @Autowired
    private IDAOTelevisore daoTelevisore;

    @Autowired
    private IDAOCalibrazione daoCalibrazione;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.seed.demo.enabled:true}")
    private boolean seedDemoAbilitato;

    @Override
    public void run(ApplicationArguments args) throws DAOException {
        if (!seedDemoAbilitato) {
            log.info("Dati demo non caricati: app.seed.demo.enabled=false.");
            return;
        }

        if (daoUtente.findAll().isEmpty()) {
            creaUtenti();
            log.info("Creati {} utenti demo", 5);
        }

        if (daoTelevisore.findAll().isEmpty()) {
            creaTelevisori();
            log.info("Create {} televisioni demo", 20);
        }
    }

    private void creaUtenti() throws DAOException {
        creaUtente("Marco", "Rossi", "admin@mediaworld.it", "Admin123!",
                ERuolo.ADMIN, true, dataGiorniFa(320));
        creaUtente("Luca", "Bianchi", "tecnico@mediaworld.it", "Tecnico123!",
                ERuolo.TECNICO, true, dataGiorniFa(280));
        creaUtente("Sara", "Conti", "sara.conti@mediaworld.it", "Tecnico123!",
                ERuolo.TECNICO, true, dataGiorniFa(190));
        creaUtente("Andrea", "Ferri", "andrea.ferri@mediaworld.it", "Tecnico123!",
                ERuolo.TECNICO, true, dataGiorniFa(140));
        creaUtente("Giulia", "Moretti", "giulia.moretti@mediaworld.it", "Tecnico123!",
                ERuolo.TECNICO, false, dataGiorniFa(90));
    }

    private void creaUtente(String nome, String cognome, String email, String password,
                            ERuolo ruolo, boolean attivo, LocalDateTime dataCreazione)
            throws DAOException {
        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setEmail(email.trim().toLowerCase(Locale.ROOT));
        utente.setPassword(passwordEncoder.encode(password));
        utente.setRuolo(ruolo);
        utente.setAttivo(attivo);
        utente.setDataCreazione(dataCreazione);
        daoUtente.makePersistent(utente);
    }

    private void creaTelevisori() throws DAOException {
        for (VoceTelevisore voce : TELEVISORI) {
            creaTelevisore(voce);
        }
    }

    private void creaTelevisore(VoceTelevisore voce) throws DAOException {
        Calibrazione calibrazione = null;

        if (voce.stato() == EStato.CALIBRATO && voce.calibrazioneGiorniFa() != null) {
            String tecnico = voce.tecnico() == null ? "Luca Bianchi" : voce.tecnico();
            calibrazione = creaCalibrazione(voce.seme(), voce.calibrazioneGiorniFa(), tecnico);
            daoCalibrazione.makePersistent(calibrazione);
        }

        Televisore televisore = new Televisore();
        televisore.setMarca(voce.marca());
        televisore.setModello(voce.modello());
        televisore.setCodiceSeriale(voce.codiceSeriale());
        televisore.setPollici(voce.pollici());
        televisore.setTecnologia(voce.tecnologia());
        televisore.setStato(voce.stato());
        televisore.setDataAggiunta(dataGiorniFa(voce.aggiuntaGiorniFa()));
        televisore.setDataCalibrazione(calibrazione == null ? null : calibrazione.getDataCalibrazione());
        televisore.setAggiuntoDa(voce.aggiuntoDa());
        televisore.setNote(voce.note() == null ? "" : voce.note());
        televisore.setCalibrazione(calibrazione);

        daoTelevisore.makePersistent(televisore);
    }

    private Calibrazione creaCalibrazione(int seme, int giorniFa, String tecnico) {
        GeneratoreCasuale casuale = new GeneratoreCasuale(seme + 31);
        EModalitaColore modalita = casuale.nextDouble() > 0.5
                ? EModalitaColore.HSL
                : EModalitaColore.RGB;

        Calibrazione calibrazione = new Calibrazione();
        calibrazione.setDataCalibrazione(dataGiorniFa(giorniFa, 11));
        calibrazione.setTecnico(tecnico);
        calibrazione.setNote(NOTE_CALIBRAZIONE[seme % NOTE_CALIBRAZIONE.length]);
        calibrazione.setValori(creaValori(seme, modalita));
        return calibrazione;
    }

    private Valori creaValori(int seme, EModalitaColore modalita) {
        GeneratoreCasuale casuale = new GeneratoreCasuale(seme);

        Colori colori = new Colori();
        colori.setRosso(creaColore(casuale.intero(230, 255), casuale.intero(0, 22), casuale.intero(0, 22)));
        colori.setVerde(creaColore(casuale.intero(0, 22), casuale.intero(215, 250), casuale.intero(0, 26)));
        colori.setBlu(creaColore(casuale.intero(0, 22), casuale.intero(0, 26), casuale.intero(220, 255)));
        colori.setCiano(creaColore(casuale.intero(0, 22), casuale.intero(205, 245), casuale.intero(220, 250)));
        colori.setMagenta(creaColore(casuale.intero(215, 250), casuale.intero(0, 26), casuale.intero(215, 250)));
        colori.setGiallo(creaColore(casuale.intero(225, 255), casuale.intero(215, 245), casuale.intero(0, 26)));

        Valori valori = new Valori();
        valori.setParametri(List.of(
                creaParametro("Luminosità", String.valueOf(casuale.intero(36, 54))),
                creaParametro("Contrasto", String.valueOf(casuale.intero(82, 96))),
                creaParametro("Nitidezza", String.valueOf(casuale.intero(0, 10))),
                creaParametro("Dettagli ombre", String.valueOf(casuale.intero(44, 56))),
                creaParametro("Gamma",
                        String.format(Locale.ROOT, "%.2f", 2.15 + casuale.nextDouble() * 0.22))));
        valori.setBiancoGain(creaRgb(casuale.intero(-8, 8), casuale.intero(-8, 8), casuale.intero(-8, 8)));
        valori.setBiancoOffset(creaRgb(casuale.intero(-4, 4), casuale.intero(-4, 4), casuale.intero(-4, 4)));
        valori.setColoriAbilitati(casuale.nextDouble() > 0.25);
        valori.setModalitaColore(modalita);
        valori.setColori(colori);
        return valori;
    }

    private Parametro creaParametro(String etichetta, String valore) {
        Parametro parametro = new Parametro();
        parametro.setEtichetta(etichetta);
        parametro.setValore(valore);
        return parametro;
    }

    private ColoreCalibrazione creaColore(int r, int g, int b) {
        Rgb rgb = creaRgb(r, g, b);
        ColoreCalibrazione colore = new ColoreCalibrazione();
        colore.setRgb(rgb);
        colore.setHsl(rgbToHsl(rgb));
        return colore;
    }

    private Rgb creaRgb(int r, int g, int b) {
        Rgb rgb = new Rgb();
        rgb.setR(r);
        rgb.setG(g);
        rgb.setB(b);
        return rgb;
    }

    private Hsl rgbToHsl(Rgb rgb) {
        double r = limita(rgb.getR(), 0, 255) / 255.0;
        double g = limita(rgb.getG(), 0, 255) / 255.0;
        double b = limita(rgb.getB(), 0, 255) / 255.0;

        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double delta = max - min;

        double h = 0;
        if (delta != 0) {
            if (max == r) {
                h = ((g - b) / delta) % 6;
            } else if (max == g) {
                h = (b - r) / delta + 2;
            } else {
                h = (r - g) / delta + 4;
            }
            h *= 60;
            if (h < 0) {
                h += 360;
            }
        }

        double l = (max + min) / 2;
        double s = delta == 0 ? 0 : delta / (1 - Math.abs(2 * l - 1));

        Hsl hsl = new Hsl();
        hsl.setH((int) Math.round(h));
        hsl.setS((int) Math.round(s * 100));
        hsl.setL((int) Math.round(l * 100));
        return hsl;
    }

    private static double limita(double valore, double minimo, double massimo) {
        return Math.min(massimo, Math.max(minimo, valore));
    }

    private static LocalDateTime dataGiorniFa(int giorni) {
        return dataGiorniFa(giorni, 10);
    }

    private static LocalDateTime dataGiorniFa(int giorni, int ora) {
        return LocalDateTime.now()
                .minusDays(giorni)
                .withHour(ora)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    private static final class GeneratoreCasuale {

        private long stato;

        private GeneratoreCasuale(int seme) {
            this.stato = (long) seme * 7919L + 104729L;
        }

        private double nextDouble() {
            stato = (stato * 9301L + 49297L) % 233280L;
            return (double) stato / 233280.0;
        }

        private int intero(int min, int max) {
            return (int) Math.floor(nextDouble() * (max - min + 1)) + min;
        }
    }

    private record VoceTelevisore(
            EMarca marca,
            String modello,
            String codiceSeriale,
            int pollici,
            ETecnologia tecnologia,
            EStato stato,
            int aggiuntaGiorniFa,
            Integer calibrazioneGiorniFa,
            String tecnico,
            String aggiuntoDa,
            String note,
            int seme) {
    }

    private static final List<VoceTelevisore> TELEVISORI = List.of(
            new VoceTelevisore(EMarca.SAMSUNG, "Neo QLED QN90D 65\"", "MW-SAM-65QN90D-0001", 65,
                    ETecnologia.NEO_QLED, EStato.CALIBRATO, 60, 35, "Luca Bianchi", "Luca Bianchi", null, 1),
            new VoceTelevisore(EMarca.LG, "OLED evo C4 55\"", "MW-LG-55C4-0002", 55,
                    ETecnologia.OLED, EStato.CALIBRATO, 45, 21, "Sara Conti", "Sara Conti", null, 2),
            new VoceTelevisore(EMarca.SONY, "Bravia 9 75\"", "MW-SONY-75BR9-0003", 75,
                    ETecnologia.MINI_LED, EStato.DA_CALIBRARE, 12, null, null, "Marco Rossi",
                    "Esposizione area premium, verificare uniformità del pannello.", 3),
            new VoceTelevisore(EMarca.PHILIPS, "OLED+ 909 65\"", "MW-PHL-65OLED909-0004", 65,
                    ETecnologia.OLED, EStato.IN_CALIBRAZIONE, 8, null, null, "Andrea Ferri", null, 4),
            new VoceTelevisore(EMarca.PANASONIC, "Z95A 65\"", "MW-PAN-65Z95A-0005", 65,
                    ETecnologia.OLED, EStato.CALIBRATO, 75, 48, "Andrea Ferri", "Andrea Ferri", null, 5),
            new VoceTelevisore(EMarca.HISENSE, "U8N 65\"", "MW-HIS-65U8N-0006", 65,
                    ETecnologia.MINI_LED, EStato.DA_CALIBRARE, 5, null, null, "Luca Bianchi", null, 6),
            new VoceTelevisore(EMarca.TCL, "C855 75\"", "MW-TCL-75C855-0007", 75,
                    ETecnologia.MINI_LED, EStato.CALIBRATO, 90, 66, "Luca Bianchi", "Luca Bianchi", null, 7),
            new VoceTelevisore(EMarca.SAMSUNG, "The Frame LS03D 55\"", "MW-SAM-55LS03D-0008", 55,
                    ETecnologia.QLED, EStato.DA_CALIBRARE, 3, null, null, "Sara Conti", null, 8),
            new VoceTelevisore(EMarca.LG, "QNED91 86\"", "MW-LG-86QNED91-0009", 86,
                    ETecnologia.MINI_LED, EStato.CALIBRATO, 110, 84, "Sara Conti", "Sara Conti", null, 9),
            new VoceTelevisore(EMarca.SONY, "Bravia 8 65\"", "MW-SONY-65BR8-0010", 65,
                    ETecnologia.OLED, EStato.IN_CALIBRAZIONE, 6, null, null, "Marco Rossi", null, 10),
            new VoceTelevisore(EMarca.PHILIPS, "Ambilight 55PUS8319", "MW-PHL-55PUS8319-0011", 55,
                    ETecnologia.LED, EStato.DA_CALIBRARE, 20, null, null, "Andrea Ferri", null, 11),
            new VoceTelevisore(EMarca.HISENSE, "E7NQ Pro 55\"", "MW-HIS-55E7NQ-0012", 55,
                    ETecnologia.QLED, EStato.CALIBRATO, 130, 105, "Andrea Ferri", "Andrea Ferri", null, 12),
            new VoceTelevisore(EMarca.TCL, "QM8B 65\"", "MW-TCL-65QM8B-0013", 65,
                    ETecnologia.MINI_LED, EStato.DA_CALIBRARE, 15, null, null, "Luca Bianchi", null, 13),
            new VoceTelevisore(EMarca.SAMSUNG, "OLED S95D 77\"", "MW-SAM-77S95D-0014", 77,
                    ETecnologia.OLED, EStato.CALIBRATO, 150, 130, "Luca Bianchi", "Luca Bianchi", null, 14),
            new VoceTelevisore(EMarca.PANASONIC, "W95A 65\"", "MW-PAN-65W95A-0015", 65,
                    ETecnologia.MINI_LED, EStato.DA_CALIBRARE, 9, null, null, "Sara Conti", null, 15),
            new VoceTelevisore(EMarca.LG, "OLED G4 65\"", "MW-LG-65G4-0016", 65,
                    ETecnologia.OLED, EStato.CALIBRATO, 40, 12, "Andrea Ferri", "Andrea Ferri", null, 16),
            new VoceTelevisore(EMarca.SONY, "Bravia 7 55\"", "MW-SONY-55BR7-0017", 55,
                    ETecnologia.MINI_LED, EStato.DA_CALIBRARE, 2, null, null, "Marco Rossi",
                    "Nuovo arrivo, da posizionare in area home cinema.", 17),
            new VoceTelevisore(EMarca.PHILIPS, "OLED 807 55\"", "MW-PHL-55OLED807-0018", 55,
                    ETecnologia.OLED, EStato.IN_CALIBRAZIONE, 4, null, null, "Luca Bianchi", null, 18),
            new VoceTelevisore(EMarca.SAMSUNG, "Neo QLED QN95D 85\"", "MW-SAM-85QN95D-0019", 85,
                    ETecnologia.NEO_QLED, EStato.CALIBRATO, 20, 4, "Sara Conti", "Sara Conti", null, 19),
            new VoceTelevisore(EMarca.LG, "OLED evo C4 77\"", "MW-LG-77C4-0020", 77,
                    ETecnologia.OLED, EStato.DA_CALIBRARE, 1, null, null, "Sara Conti", null, 20));

}
