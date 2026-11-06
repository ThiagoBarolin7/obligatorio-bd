package ude.obligatorio.logica;

import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.module.Configuration;
import java.util.List;
import java.util.Properties;

import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.logica.valueObjects.VOActividad;
import ude.obligatorio.logica.valueObjects.VORegistro;
import ude.obligatorio.logica.valueObjects.VORegistroListado;
import ude.obligatorio.persistencia.daos.DAORegistros;
import ude.obligatorio.persistencia.daos.IDAORegistros;
import ude.obligatorio.persistencia.fabrica.FabricaAbstracta;
import ude.obligatorio.persistencia.poolConexiones.IConexion;

public class Alumno implements Serializable {

    private static final long serialVersionUID = 1L;

    private int cedula;
    private String nombre;
    private String apellido;
    private IDAORegistros secuencia;

    public Alumno(int cedula, String nombre, String apellido) throws PersistenciaException {
        this.cedula = cedula;
        this.nombre = nombre;
        this.apellido = apellido;
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("config.properties"));
            FabricaAbstracta fabrica = (FabricaAbstracta) Class.forName(p.getProperty("fabricaConcreta")).newInstance();
            this.secuencia = fabrica.crearIDAORegistros(cedula);
        } catch(Exception e) {
            throw new PersistenciaException("Error al cargar datos del alumno");
        }
    }

    public int getCedula() {
        return cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int cantidadRegistros(IConexion icon) throws PersistenciaException {
        return secuencia.largo(icon);
    }

    public void agregarRegistro(IConexion icon, Registro reg) throws PersistenciaException {
        secuencia.insback(icon, reg);
    }

    public VORegistro obtenRegistro(IConexion icon, int numR) throws PersistenciaException {
        Registro reg = secuencia.kesimo(icon, numR);
        if (reg == null) 
            return null;

        return new VORegistro(reg.getActividad(), reg.getNota());
    }

    public List<VORegistroListado> listarRegistros(IConexion icon) throws PersistenciaException {
        return secuencia.listarRegistros(icon);
    }

    public double promedioNotas(IConexion icon) throws PersistenciaException {
        return secuencia.promedioNotas(icon);
    }

    public VOActividad actividadMasCursada(IConexion icon) throws PersistenciaException {
        return secuencia.actividadMasCursada(icon);
    }
}
