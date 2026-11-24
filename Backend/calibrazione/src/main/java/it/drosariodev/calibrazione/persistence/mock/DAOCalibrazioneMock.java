package it.drosariodev.calibrazione.persistence.mock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import it.drosariodev.calibrazione.model.Calibrazione;
import it.drosariodev.calibrazione.persistence.IDAOCalibrazione;

@Repository
@Profile("mock")
public class DAOCalibrazioneMock extends DAOGenericMock<Calibrazione> implements IDAOCalibrazione {

}
