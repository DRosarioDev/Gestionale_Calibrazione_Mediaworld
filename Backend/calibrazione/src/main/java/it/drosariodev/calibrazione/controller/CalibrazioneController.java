package it.drosariodev.calibrazione.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.drosariodev.calibrazione.dto.televisore.CalibrazioneDTO;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.service.CalibrazioneService;

/**
 * API di consultazione delle calibrazioni.
 * Il salvataggio resta su PUT /api/televisori/{id}/calibrazione, come da contratto.
 */
@RestController
@RequestMapping("/api/calibrazioni")
public class CalibrazioneController {

    @Autowired
    private CalibrazioneService calibrazioneService;

    @GetMapping
    public List<CalibrazioneDTO> elenco() throws DAOException {
        return calibrazioneService.findAll();
    }

    @GetMapping("/televisore/{televisoreId}")
    public CalibrazioneDTO perTelevisore(@PathVariable Long televisoreId) throws DAOException {
        return calibrazioneService.findByTelevisore(televisoreId);
    }

    @GetMapping("/{id}")
    public CalibrazioneDTO dettaglio(@PathVariable Long id) throws DAOException {
        return calibrazioneService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) throws DAOException {
        calibrazioneService.elimina(id);
    }

}
