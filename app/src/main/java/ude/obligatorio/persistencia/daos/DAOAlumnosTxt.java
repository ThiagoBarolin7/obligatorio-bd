package ude.obligatorio.persistencia.daos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import ude.obligatorio.logica.Alumno;
import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.persistencia.consultas.Consultas;
import ude.obligatorio.persistencia.poolConexiones.Conexion;
import ude.obligatorio.persistencia.poolConexiones.ConexionTxt;
import ude.obligatorio.persistencia.poolConexiones.IConexion;
import ude.obligatorio.logica.valueObjects.VORegistroListado;
import ude.obligatorio.logica.valueObjects.VOAlumnoProm;    

public class DAOAlumnosTxt implements IDAOAlumnos {

    @Override
    public boolean member(IConexion icon, int ced) throws PersistenciaException {
        boolean existeCedula = false;
        ConexionTxt con = (ConexionTxt) icon;
        String linea;

        try (BufferedReader reader = con.getArchivoAlumnos()) {
            while (!existeCedula && (linea = reader.readLine()) != null) {
                String cedula = linea.split("\\|")[0].trim();
                existeCedula = cedula.equals(String.valueOf(ced));
            }
        } catch (IOException e) {
            throw new PersistenciaException("Error de acceso a los datos");
        }

        return existeCedula;
    }

    @Override 
    public void insert(IConexion icon, Alumno alumno) throws PersistenciaException {
        ConexionTxt con = (ConexionTxt) icon;
        
        List<Alumno> alumnos = leerAlumnos(con);
        alumnos.add(alumno);
        alumnos.sort (Comparator.comparingInt(Alumno::getCedula));
        
        try (BufferedWriter writer = con.getArchivoAlumnosEscritura()) {
            for (Alumno a : alumnos) {
                writer.write(a.getCedula() + " | " + a.getNombre() + " | " + a.getApellido());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenciaException("Error de acceso a los datos");
        }
    }

    @Override 
    public Alumno find(IConexion icon, int cedA) throws PersistenciaException {
        Alumno alumno = null;
        ConexionTxt con = (ConexionTxt) icon;

        try (BufferedReader reader = con.getArchivoAlumnos()) {
            String linea;
            while (alumno == null && (linea = reader.readLine()) != null) {
                Alumno a = leerAlumno(linea);
                if (a != null && a.getCedula() == cedA) {
                    alumno = a;
            }
            }
        } catch (IOException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new PersistenciaException("Error de acceso a los datos");
        }
        
        return alumno;
    }

    @Override 
    public boolean esVacio(IConexion icon) throws PersistenciaException {
        return leerAlumnos((ConexionTxt) icon).isEmpty();
    }

    @Override 
    public List<Alumno> listarAlumnos(IConexion icon) throws PersistenciaException {
        ConexionTxt con = (ConexionTxt) icon;
        return leerAlumnos(con);
    }

    @Override 
    public VOAlumnoProm alumnoMayorPromedio(IConexion icon) throws PersistenciaException {
        Alumno mejor = null;
        double mayor = 0;

        for (Alumno a : leerAlumnos((ConexionTxt) icon)) {
            double prom = new DAORegistrosTxt(a.getCedula()).promedioNotas(icon);

            if(mejor == null || prom > mayor) {
                mejor = a;
                mayor = prom;
            }
        }

        return mejor != null 
            ? new VOAlumnoProm(mejor.getCedula(), mejor.getNombre(), mejor.getApellido(), mayor)
            : null;
    }

    @Override 
    public int contarAlumnos(IConexion icon, String act, int nota) throws PersistenciaException {
        int cant = 0;

        for (Alumno a : leerAlumnos((ConexionTxt) icon)) {
            for (VORegistroListado r : a.listarRegistros(icon)) {
                if (r.getActividad().equalsIgnoreCase(act) && r.getNota() == nota) {
                    cant++;
                    break;
                }
            }
        }
        return cant;
    }

    // Auxiliares

    private List<Alumno> leerAlumnos (ConexionTxt con) throws PersistenciaException {
        List<Alumno> lista = new ArrayList<Alumno>();

        try (BufferedReader reader = con.getArchivoAlumnos()) {
           String linea;

           while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) {
                    lista.add(leerAlumno(linea));
                }
           }
        } catch (IOException e) {
            throw new PersistenciaException("Error de acceso a los datos");
        }
        return lista;
    }

    private Alumno leerAlumno (String linea) throws PersistenciaException {
        Alumno alumno = null;

        if (linea != null && !linea.isBlank()) {
            try {
                String[] campos = linea.split("\\|");
                int cedula = Integer.parseInt(campos[0].trim());
                alumno = new Alumno(cedula, campos[1].trim(), campos[2].trim());
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                throw new PersistenciaException("Error al leer datos");
            }
        }
        
        return alumno;
    }


}
