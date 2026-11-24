package it.drosariodev.calibrazione.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import it.drosariodev.calibrazione.dto.utente.AggiornaStatoUtenteRequest;
import it.drosariodev.calibrazione.dto.utente.NuovoUtenteRequest;
import it.drosariodev.calibrazione.dto.utente.UtenteDTO;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.service.UtenteService;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/utenti")
public class UtenteController {
    
    @Autowired 
    private UtenteService utenteService;

    @GetMapping
    public List<UtenteDTO> elenco() throws DAOException {
        return utenteService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtenteDTO crea(@Valid @RequestBody NuovoUtenteRequest request) throws DAOException {
        return utenteService.crea(request);
    }

    @PatchMapping("/{id}/stato")
    public UtenteDTO aggiornaStato(@PathVariable Long id,
                                   @Valid @RequestBody AggiornaStatoUtenteRequest request) throws DAOException {
        return utenteService.aggiornaStato(id, request.getAttivo());
    }

}
