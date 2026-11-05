package ude.obligatorio.persistencia.daos;

import java.util.List;

import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.persistencia.poolConexiones.IConexion;
import ude.obligatorio.logica.Registro;
import ude.obligatorio.logica.valueObjects.VOActividad;
import ude.obligatorio.logica.valueObjects.VORegistroListado;

public interface IDAORegistros {

    int largo(IConexion icon) throws PersistenciaException;

    void insback(IConexion icon, Registro reg) throws PersistenciaException;

    Registro kesimo(IConexion icon, int numR) throws PersistenciaException;

    List<VORegistroListado> listarRegistros(IConexion icon) throws PersistenciaException;

    double promedioNotas(IConexion icon) throws PersistenciaException;

    VOActividad actividadMasCursada(IConexion icon) throws PersistenciaException;
}