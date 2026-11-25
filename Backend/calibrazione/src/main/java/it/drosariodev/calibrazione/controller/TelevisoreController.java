package it.drosariodev.calibrazione.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.drosariodev.calibrazione.dto.televisore.NuovoTelevisoreRequest;
import it.drosariodev.calibrazione.dto.televisore.SalvaCalibrazioneRequest;
import it.drosariodev.calibrazione.dto.televisore.SalvaReportRequest;
import it.drosariodev.calibrazione.dto.televisore.TelevisoreDTO;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.service.TelevisoreService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/televisori")
public class TelevisoreController {

    @Autowired
    private TelevisoreService televisoreService;

    @GetMapping
    public List<TelevisoreDTO> elenco() throws DAOException {
        return televisoreService.findAll();
    }

    @GetMapping("/{id}")
    public TelevisoreDTO dettaglio(@PathVariable Long id) throws DAOException {
        return televisoreService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TelevisoreDTO crea(@Valid @RequestBody NuovoTelevisoreRequest request) throws DAOException {
        return televisoreService.crea(request);
    }

    @PatchMapping("/{id}/avvia-calibrazione")
    public TelevisoreDTO avviaCalibrazione(@PathVariable Long id) throws DAOException {
        return televisoreService.avviaCalibrazione(id);
    }

    @PutMapping("/{id}/calibrazione")
    public TelevisoreDTO salvaCalibrazione(@PathVariable Long id,
                                           @Valid @RequestBody SalvaCalibrazioneRequest request) throws DAOException {
        return televisoreService.salvaCalibrazione(id, request);
    }

    @PutMapping("/{id}/calibrazione/report")
    public TelevisoreDTO salvaReport(@PathVariable Long id,
                                     @Valid @RequestBody SalvaReportRequest request) throws DAOException {
        return televisoreService.salvaReport(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void elimina(@PathVariable Long id) throws DAOException {
        televisoreService.elimina(id);
    }

}
