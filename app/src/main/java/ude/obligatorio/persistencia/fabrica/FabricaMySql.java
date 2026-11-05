package ude.obligatorio.persistencia.fabrica;

import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.persistencia.daos.DAOAlumnosTxt;
import ude.obligatorio.persistencia.daos.DAORegistros;
import ude.obligatorio.persistencia.daos.IDAOAlumnos;
import ude.obligatorio.persistencia.daos.IDAORegistros;

public class FabricaMySql implements FabricaAbstracta {
    
    
    @Override 
    public IDAOAlumnos crearIDAOAlumnos() throws PersistenciaException {
        return new DAOAlumnosTxt();
    }

    @Override 
    public IDAORegistros crearIDAORegistros(int cedAlumno) throws PersistenciaException{
        return new DAORegistros(cedAlumno);
    }
}

