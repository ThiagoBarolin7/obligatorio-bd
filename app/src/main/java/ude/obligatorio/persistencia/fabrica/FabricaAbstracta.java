package ude.obligatorio.persistencia.fabrica;

import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.persistencia.daos.IDAOAlumnos;
import ude.obligatorio.persistencia.daos.IDAORegistros;

public interface FabricaAbstracta {
    
    IDAOAlumnos crearIDAOAlumnos() throws PersistenciaException;

    IDAORegistros crearIDAORegistros(int cedAlumno) throws PersistenciaException;
}
