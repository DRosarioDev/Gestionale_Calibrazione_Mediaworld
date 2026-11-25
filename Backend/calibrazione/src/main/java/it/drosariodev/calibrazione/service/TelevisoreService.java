package it.drosariodev.calibrazione.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import it.drosariodev.calibrazione.dto.televisore.NuovoTelevisoreRequest;
import it.drosariodev.calibrazione.dto.televisore.SalvaCalibrazioneRequest;
import it.drosariodev.calibrazione.dto.televisore.SalvaReportRequest;
import it.drosariodev.calibrazione.dto.televisore.TelevisoreDTO;
import it.drosariodev.calibrazione.enums.EStato;
import it.drosariodev.calibrazione.model.Televisore;
import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOTelevisore;
import it.drosariodev.calibrazione.util.Mapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TelevisoreService {

    @Autowired
    private IDAOTelevisore daoTelevisore;

    @Autowired
    private CalibrazioneService calibrazioneService;

    @Autowired
    private UtenteCorrenteService utenteCorrenteService;

    public List<TelevisoreDTO> findAll() throws DAOException {
        return Mapper.map(daoTelevisore.findAll(), TelevisoreDTO.class);
    }

    public TelevisoreDTO findById(Long id) throws DAOException {
        return Mapper.map(trovaTelevisore(id), TelevisoreDTO.class);
    }

    public TelevisoreDTO crea(NuovoTelevisoreRequest request) throws DAOException {
        Televisore televisore = Mapper.map(request, Televisore.class);

        if (televisore.getMarca() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca non valida.");
        }
        if (televisore.getTecnologia() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tecnologia non valida.");
        }

        // Il codice seriale e' facoltativo: se assente viene salvato come stringa
        // vuota, che l'indice univoco parziale ignora.
        String codiceSeriale = request.getCodiceSeriale() == null
                ? ""
                : request.getCodiceSeriale().trim().toUpperCase(Locale.ROOT);
        if (!codiceSeriale.isEmpty() && daoTelevisore.findByCodiceSeriale(codiceSeriale) != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esiste già una televisione con questo codice seriale.");
        }

        Utente utente = utenteCorrenteService.corrente();

        televisore.setCodiceSeriale(codiceSeriale);
        televisore.setNote(request.getNote() == null ? "" : request.getNote().trim());
        televisore.setStato(EStato.DA_CALIBRARE);
        televisore.setDataAggiunta(LocalDateTime.now());
        televisore.setDataCalibrazione(null);
        televisore.setAggiuntoDa(utenteCorrenteService.nomeCompleto(utente));
        televisore.setCalibrazione(null);

        daoTelevisore.makePersistent(televisore);
        log.info("Aggiunta televisione {} {} con seriale {}",
                televisore.getMarca(), televisore.getModello(), televisore.getCodiceSeriale());

        return Mapper.map(televisore, TelevisoreDTO.class);
    }

    public TelevisoreDTO avviaCalibrazione(Long id) throws DAOException {
        Televisore televisore = trovaTelevisore(id);

        if (televisore.getStato() == EStato.CALIBRATO) {
            // throw new ResponseStatusException(
            //         HttpStatus.CONFLICT,
            //         "La televisione è già stata calibrata.");
            televisore.setStato(EStato.IN_CALIBRAZIONE);
            return Mapper.map(televisore, TelevisoreDTO.class);
        }

        televisore.setStato(EStato.IN_CALIBRAZIONE);
        daoTelevisore.makePersistent(televisore);
        log.info("Avviata calibrazione della televisione con seriale {}", televisore.getCodiceSeriale());

        return Mapper.map(televisore, TelevisoreDTO.class);
    }

    public TelevisoreDTO salvaCalibrazione(Long id, SalvaCalibrazioneRequest request) throws DAOException {
        calibrazioneService.salva(id, request);
        return Mapper.map(trovaTelevisore(id), TelevisoreDTO.class);
    }

    public TelevisoreDTO salvaReport(Long id, SalvaReportRequest request) throws DAOException {
        calibrazioneService.salvaReport(id, request.getReport());
        return Mapper.map(trovaTelevisore(id), TelevisoreDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void elimina(Long id) throws DAOException {
        Televisore televisore = trovaTelevisore(id);

        calibrazioneService.eliminaPerTelevisore(televisore);

        daoTelevisore.makeTransient(televisore);
        log.info("Eliminata televisione con seriale {}", televisore.getCodiceSeriale());
    }

    private Televisore trovaTelevisore(Long id) throws DAOException {
        Televisore televisore = daoTelevisore.findById(id);
        if (televisore == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Televisione non trovata.");
        }
        return televisore;
    }

}
