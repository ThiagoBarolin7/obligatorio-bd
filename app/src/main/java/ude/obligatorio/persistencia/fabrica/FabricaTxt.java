package ude.obligatorio.persistencia.fabrica;

import ude.obligatorio.persistencia.daos.IDAOAlumnos;
import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.persistencia.daos.DAOAlumnosTxt;
import ude.obligatorio.persistencia.daos.IDAORegistros;
import ude.obligatorio.persistencia.daos.DAORegistrosTxt;

public class FabricaTxt implements FabricaAbstracta {
    
    @Override 
    public IDAOAlumnos crearIDAOAlumnos() throws PersistenciaException {
        return new DAOAlumnosTxt();
    }

    @Override 
    public IDAORegistros crearIDAORegistros(int cedAlumno) throws PersistenciaException{
        return new DAORegistrosTxt(cedAlumno);
    }
}
