package it.drosariodev.calibrazione.service;

import org.springframework.stereotype.Service;

import it.drosariodev.calibrazione.model.Utente;
import it.drosariodev.calibrazione.persistence.DAOException;
import it.drosariodev.calibrazione.persistence.IDAOUtente;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IDAOUtente daoUtente;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            Utente utente = daoUtente.findByEmail(email);
            if (utente == null) {
                throw new UsernameNotFoundException("User not found: " + email);
            }
            return org.springframework.security.core.userdetails.User
                .withUsername(utente.getEmail())
                .password(utente.getPassword())
                .roles(utente.getRuolo().name())
                .disabled(!utente.isAttivo())
                .build();
        } catch (DAOException ex) {
            throw new UsernameNotFoundException("Impossible to find the user: " + email, ex);
        }
    }
    // PER DB

    // @Override
    // public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    //     try {
    //         DAOUtilHibernate .beginTransaction();
    //         User user = daoUser.getUserByEmail(email);
    //         DAOUtilHibernate.commitTransaction();
    //         if (user == null) {
    //             throw new UsernameNotFoundException("User not found: " + email);
    //         }
    //         return org.springframework.security.core.userdetails.User
    //             .withUsername(user.getEmail())
    //             .password(user.getPassword())
    //             .roles(user.getRole().name())
    //             .build();
    //     } catch (DAOException ex) {
    //         DAOUtilHibernate.rollbackTransaction();
    //         throw new UsernameNotFoundException("Impossible to find the user: " + email, ex);
    //     }
    // }
}