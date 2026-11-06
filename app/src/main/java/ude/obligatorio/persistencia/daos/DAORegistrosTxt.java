package ude.obligatorio.persistencia.daos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ude.obligatorio.logica.Registro;
import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.logica.valueObjects.VOActividad;
import ude.obligatorio.logica.valueObjects.VORegistroListado;
import ude.obligatorio.persistencia.poolConexiones.ConexionTxt;
import ude.obligatorio.persistencia.poolConexiones.IConexion;

public class DAORegistrosTxt implements IDAORegistros {

    private int cedAlumno;

    public DAORegistrosTxt(int cedAlumno) throws PersistenciaException {
        this.cedAlumno = cedAlumno;
    }
    
    @Override 
    public int largo(IConexion icon) throws PersistenciaException {
        return leerRegistros(icon).size();
    }

    @Override 
    public void insback(IConexion icon, Registro reg) throws PersistenciaException {
        ConexionTxt con = (ConexionTxt) icon;

        try (BufferedWriter writer = con.getArchivoRegistrosAgregar(cedAlumno)) {
            writer.write(reg.getNumero() + " | " + reg.getActividad() + " | " + reg.getNota());
            writer.newLine();
        } catch (IOException e) {
            throw new PersistenciaException("Error al escribir registro en archivo");
        }
    }

    @Override 
    public Registro kesimo(IConexion icon, int numR) throws PersistenciaException {
        List<Registro> lista = leerRegistros(icon);
        Registro registro = null;

        if (numR >= 1 && numR <= lista.size()) {
            registro = lista.get(numR - 1);
        }
        
        return registro;
    }

    @Override 
    public List<VORegistroListado> listarRegistros(IConexion icon) throws PersistenciaException {
        List<VORegistroListado> lista = new ArrayList<VORegistroListado>();

        for (Registro reg : leerRegistros(icon)) {
            lista.add(new VORegistroListado(reg.getNumero(), reg.getNota(), reg.getActividad()));
        }

        return lista;
    }

    @Override 
    public double promedioNotas(IConexion icon) throws PersistenciaException {
        List<Registro> registros = leerRegistros(icon);
        
        if (registros.isEmpty()) 
            return 0;
      
        double sumaNotas = registros.stream()
            .mapToDouble(Registro::getNota)
            .sum();

        return sumaNotas / registros.size();
    }

    @Override 
    public VOActividad actividadMasCursada(IConexion icon) throws PersistenciaException {
        Map<String, Integer> veces = new LinkedHashMap<String, Integer>();

        for (Registro r : leerRegistros(icon)) {
            veces.merge(r.getActividad(), 1, Integer::sum);
        }
        
        VOActividad masCursada = null;

        for (Map.Entry<String, Integer> e : veces.entrySet()) {
            if (masCursada == null || e.getValue() > masCursada.getVeces()) {
                masCursada = new VOActividad(e.getKey(), e.getValue());
            }
        }
        
        return masCursada;
    }

    private Registro leerRegistro(String linea) throws PersistenciaException {
        Registro registro = null;

        if (linea != null && !linea.isBlank()) {
            try {
                String[] campos = linea.split("\\|");
                int numero = Integer.parseInt(campos[0].trim());
                String materia = campos[1].trim();
                int nota = Integer.parseInt(campos[2].trim());
                registro = new Registro(numero, nota, materia);
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                throw new PersistenciaException("Error al leer datos");
            }
        }
        
        return registro;
    }

    private List<Registro> leerRegistros(IConexion icon) throws PersistenciaException {
        ConexionTxt con = (ConexionTxt) icon;
        List<Registro> lista = new ArrayList<Registro>();
        
        try (BufferedReader reader = con.getArchivoRegistros(cedAlumno)) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) {
                    lista.add(leerRegistro(linea));
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException ("Error al leer registros del archivo");
        }

        lista.sort (Comparator.comparingInt(Registro::getNumero));
        return lista;
    }
}
