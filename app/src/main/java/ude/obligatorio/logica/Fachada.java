package ude.obligatorio.logica;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import ude.obligatorio.logica.excepciones.AlumnoException;
import ude.obligatorio.logica.excepciones.NotasException;
import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.logica.excepciones.RegistroException;
import ude.obligatorio.logica.valueObjects.VOActividad;
import ude.obligatorio.logica.valueObjects.VOAlumno;
import ude.obligatorio.logica.valueObjects.VOAlumnoProm;
import ude.obligatorio.logica.valueObjects.VORegistro;
import ude.obligatorio.logica.valueObjects.VORegistroListado;
import ude.obligatorio.persistencia.daos.*;
import ude.obligatorio.persistencia.poolConexiones.IConexion;
import ude.obligatorio.persistencia.poolConexiones.IPoolConexiones;
import ude.obligatorio.persistencia.daos.DAOAlumnos;
import ude.obligatorio.persistencia.fabrica.FabricaAbstracta;


/**
 * Fachada de acceso a la capa de logica y persistencia.
 *
 * Cada requerimiento es una transaccion: pide una IConexion al pool, la usa
 * y la devuelve haciendo commit si todo salio bien o rollback si hubo un error.
 */
public class Fachada extends UnicastRemoteObject implements IFachada {

    private static final long serialVersionUID = 1L;
    private final IPoolConexiones ipool;
    private final IDAOAlumnos dao;



    public Fachada() throws RemoteException, PersistenciaException {
        super();

        try {
            Properties p = new Properties();
            p.load(new FileInputStream("config.properties"));

            ipool = (IPoolConexiones) Class.forName(p.getProperty("poolConcreto")).newInstance();
            FabricaAbstracta fabrica = (FabricaAbstracta) Class.forName(p.getProperty("fabricaConcreta")).newInstance();
            dao = fabrica.crearIDAOAlumnos();
        } catch (Exception e) {
            throw new PersistenciaException("Error al cargar pool de conexiones: " + e);
        }
    }

    // ------------------------------------------------------------------
    // Requerimientos
    // ------------------------------------------------------------------

    @Override
    public void nuevoAlumno(VOAlumno voA)
            throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(true);
        boolean ok = false;
        try {
            if (dao.member(icon, voA.getCedula())) {
                throw new AlumnoException("Ya existe un alumno con la cedula " + voA.getCedula());
            }

            dao.insert(icon, new Alumno(voA.getCedula(), voA.getNombre(), voA.getApellido()));
            ok = true;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
        public void registrarNota(int cedA, VORegistro voR) throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(true);
        boolean ok = false;
        try {
            if (!dao.member(icon, cedA)) {
                throw new AlumnoException("No existe un alumno con la cedula " + cedA);
            }

            Alumno alu = dao.find(icon, cedA);

            int numero = alu.cantidadRegistros(icon) + 1;
            Registro reg = new Registro(numero, voR.getNota(), voR.getActividad());
            alu.agregarRegistro(icon, reg);
            ok = true;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
    public List<VOAlumno> listarAlumnos() throws PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            List<VOAlumno> lista = dao.listarAlumnos(icon).stream()
                    .map(a -> new VOAlumno(
                            a.getCedula(),
                            a.getNombre(),
                            a.getApellido()))
                    .toList();
            ok = true;
            return lista;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
        public VORegistro obtenerRegistro(int cedA, int numR) throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            if (!dao.member(icon, cedA)) {
                throw new AlumnoException("No existe un alumno con la cedula " + cedA);
            }

            Alumno alu = dao.find(icon, cedA);

            if (numR < 1 || numR > alu.cantidadRegistros(icon)) {
                throw new RegistroException("El alumno " + cedA + " no tiene un registro con numero " + numR);
            }

            VORegistro voR = alu.obtenRegistro(icon, numR);
            ok = true;
            return voR;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
    public List<VORegistroListado> listarRegistros(int cedA) throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            if (!dao.member(icon, cedA)) {
                throw new AlumnoException("No existe un alumno con la cedula " + cedA);
            }

            List<VORegistroListado> lista = dao.find(icon, cedA).listarRegistros(icon);
            ok = true;
            return lista;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
    public double promedioNotas(int cedA) throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            if (!dao.member(icon, cedA)) {
                throw new AlumnoException("No existe un alumno con la cedula " + cedA);
            }

            Alumno alu = dao.find(icon, cedA);
            double promedio = alu.promedioNotas(icon);
            ok = true;
            return promedio;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
    public VOAlumnoProm alumnoMayorPromedio() throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            if (dao.esVacio(icon)) {
                throw new AlumnoException("No hay alumnos registrados");
            }

            VOAlumnoProm voA = dao.alumnoMayorPromedio(icon);
            ok = true;
            return voA;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
    public int contarAlumnos(String act, int nota) throws PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            int cant = dao.contarAlumnos(icon, act, nota);
            ok = true;
            return cant;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }

    @Override
    public VOActividad actividadMasCursada(int cedA) throws NotasException, PersistenciaException, RemoteException {
        IConexion icon = ipool.obtenerConexion(false);
        boolean ok = false;
        try {
            if (!dao.member(icon, cedA)) {
                throw new AlumnoException("No existe un alumno con la cedula " + cedA);
            }

            Alumno alu = dao.find(icon, cedA);

            if (alu.cantidadRegistros(icon) == 0) {
                throw new RegistroException("El alumno " + cedA + " no tiene ninguna actividad registrada");
            }

            VOActividad voAct = alu.actividadMasCursada(icon);
            ok = true;
            return voAct;
        } finally {
            ipool.liberarConexion(icon, ok);
        }
    }
}