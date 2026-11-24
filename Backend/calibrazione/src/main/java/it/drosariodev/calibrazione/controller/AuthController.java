package it.drosariodev.calibrazione.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.drosariodev.calibrazione.dto.auth.LoginRequest;
import it.drosariodev.calibrazione.dto.auth.LoginResponse;
import it.drosariodev.calibrazione.dto.utente.UtenteDTO;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) throws DAOException {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UtenteDTO me() throws DAOException {
        return authService.utenteCorrente();
    }

}
