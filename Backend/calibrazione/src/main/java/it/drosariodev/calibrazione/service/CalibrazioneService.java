package it.drosariodev.calibrazione.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.drosariodev.calibrazione.dto.televisore.CalibrazioneDTO;
import it.drosariodev.calibrazione.dto.televisore.ParametroCalibrazioneDTO;
import it.drosariodev.calibrazione.dto.televisore.SalvaCalibrazioneRequest;
import it.drosariodev.calibrazione.enums.EStato;
import it.drosariodev.calibrazione.model.Calibrazione;
import it.drosariodev.calibrazione.model.Parametro;
import it.drosariodev.calibrazione.model.Televisore;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.model.Valori;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOCalibrazione;
import it.drosariodev.calibrazione.persistence.IDAOTelevisore;
import it.drosariodev.calibrazione.util.Mapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Logica del dominio calibrazione.
 * L'endpoint ufficiale del contratto resta PUT /api/televisori/{id}/calibrazione,
 * che delega a questo service.
 */
@Service
@Slf4j
public class CalibrazioneService {

    @Autowired
    private IDAOCalibrazione daoCalibrazione;

    @Autowired
    private IDAOTelevisore daoTelevisore;

    @Autowired
    private UtenteCorrenteService utenteCorrenteService;

    public List<CalibrazioneDTO> findAll() throws DAOException {
        return Mapper.map(daoCalibrazione.findAll(), CalibrazioneDTO.class);
    }

    public CalibrazioneDTO findById(Long id) throws DAOException {
        return Mapper.map(trovaCalibrazione(id), CalibrazioneDTO.class);
    }

    public CalibrazioneDTO findByTelevisore(Long televisoreId) throws DAOException {
        Televisore televisore = trovaTelevisore(televisoreId);
        Calibrazione calibrazione = televisore.getCalibrazione();
        if (calibrazione == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "La televisione non è ancora stata calibrata.");
        }
        return Mapper.map(calibrazione, CalibrazioneDTO.class);
    }

    /**
     * Salva (o aggiorna) la calibrazione della televisione indicata.
     * tecnico e dataCalibrazione sono sempre calcolati dal backend.
     * L'operazione e' transazionale: calibrazione e televisione vengono
     * aggiornate insieme oppure non viene salvato nulla.
     */
    @Transactional(rollbackFor = Exception.class)
    public Calibrazione salva(Long televisoreId, SalvaCalibrazioneRequest request) throws DAOException {
        Televisore televisore = trovaTelevisore(televisoreId);
        Utente utente = utenteCorrenteService.corrente();

        Calibrazione calibrazione = televisore.getCalibrazione();
        if (calibrazione == null) {
            calibrazione = new Calibrazione();
        }
        Mapper.map(request, calibrazione);

        // ModelMapper aggiorna le liste esistenti per indice: per evitare che i
        // parametri rimossi restino salvati, la lista viene sostituita in blocco.
        Valori valori = calibrazione.getValori();
        if (valori == null) {
            valori = new Valori();
            calibrazione.setValori(valori);
        }
        valori.setParametri(mappaParametri(request.getValori().getParametri()));
        valori.setColoriAbilitati(request.getValori().isColoriAbilitati());

        LocalDateTime dataCalibrazione = LocalDateTime.now();
        calibrazione.setDataCalibrazione(dataCalibrazione);
        calibrazione.setTecnico(utenteCorrenteService.nomeCompleto(utente));
        calibrazione.setNote(request.getNote() == null ? "" : request.getNote().trim());

        daoCalibrazione.makePersistent(calibrazione);

        televisore.setCalibrazione(calibrazione);
        televisore.setDataCalibrazione(dataCalibrazione);
        televisore.setStato(EStato.CALIBRATO);
        daoTelevisore.makePersistent(televisore);

        log.info("Salvata calibrazione {} della televisione con seriale {} dal tecnico {}",
                calibrazione.getId(), televisore.getCodiceSeriale(), calibrazione.getTecnico());

        return calibrazione;
    }

    @Transactional(rollbackFor = Exception.class)
    public void elimina(Long id) throws DAOException {
        Calibrazione calibrazione = trovaCalibrazione(id);
        Televisore televisore = trovaTelevisoreDellaCalibrazione(id);

        if (televisore != null) {
            televisore.setCalibrazione(null);
            televisore.setDataCalibrazione(null);
            if (televisore.getStato() == EStato.CALIBRATO) {
                televisore.setStato(EStato.DA_CALIBRARE);
            }
            daoTelevisore.makePersistent(televisore);
        }

        daoCalibrazione.makeTransient(calibrazione);
        log.info("Eliminata calibrazione {}", id);
    }

    /**
     * Usato quando viene eliminata la televisione: rimuove la calibrazione collegata.
     */
    public void eliminaPerTelevisore(Televisore televisore) throws DAOException {
        Calibrazione calibrazione = televisore.getCalibrazione();
        if (calibrazione != null && calibrazione.getId() != null) {
            daoCalibrazione.makeTransient(calibrazione);
        }
    }

    private List<Parametro> mappaParametri(List<ParametroCalibrazioneDTO> parametri) {
        List<Parametro> mappati = new ArrayList<>();
        if (parametri == null) {
            return mappati;
        }
        for (ParametroCalibrazioneDTO dto : parametri) {
            Parametro parametro = new Parametro();
            parametro.setEtichetta(dto.getEtichetta() == null ? "" : dto.getEtichetta().trim());
            parametro.setValore(dto.getValore() == null ? "" : dto.getValore().trim());
            mappati.add(parametro);
        }
        return mappati;
    }

    private Calibrazione trovaCalibrazione(Long id) throws DAOException {
        Calibrazione calibrazione = daoCalibrazione.findById(id);
        if (calibrazione == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Calibrazione non trovata.");
        }
        return calibrazione;
    }

    private Televisore trovaTelevisore(Long id) throws DAOException {
        Televisore televisore = daoTelevisore.findById(id);
        if (televisore == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Televisione non trovata.");
        }
        return televisore;
    }

    private Televisore trovaTelevisoreDellaCalibrazione(Long calibrazioneId) throws DAOException {
        return daoTelevisore.findAll().stream()
                .filter(televisore -> televisore.getCalibrazione() != null
                        && Objects.equals(televisore.getCalibrazione().getId(), calibrazioneId))
                .findFirst()
                .orElse(null);
    }

}
