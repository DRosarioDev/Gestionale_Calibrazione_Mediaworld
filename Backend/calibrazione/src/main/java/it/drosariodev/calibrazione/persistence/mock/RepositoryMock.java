package it.drosariodev.calibrazione.persistence.mock;


import java.time.LocalDateTime;


public class RepositoryMock extends RepositoryGenericMock {

    private static final RepositoryMock singleton = new RepositoryMock();


    private RepositoryMock() {
        


    }


    public static RepositoryMock getInstance() {
        return singleton;
    }

}