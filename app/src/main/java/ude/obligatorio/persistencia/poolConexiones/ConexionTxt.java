package ude.obligatorio.persistencia.poolConexiones;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import ude.obligatorio.logica.excepciones.PersistenciaException;

public class ConexionTxt implements IConexion {

    private static final String ALUMNOS = "alumnos.txt";
    private static final String REGISTROS = "registros%s.txt";

    private final Path directorio;
    private final boolean modifica;

    private final Map<Path, Path> temporales = new HashMap<>();

    public ConexionTxt(Path directorio, boolean modifica) {
        this.directorio = directorio;
        this.modifica = modifica;
    }

    public boolean modifica() {
        return modifica;
    }

    public BufferedReader getArchivoAlumnos() throws PersistenciaException {
        try {
            return abrirLectura(ALUMNOS);
        } catch (IOException e) {
            throw new PersistenciaException("Error al obtener el archivo");
        }
    }

    public BufferedWriter getArchivoAlumnosEscritura() throws PersistenciaException {
        if (!modifica) {
            throw new PersistenciaException("No se permite escribir el archivo");
        }

        try {
            return Files.newBufferedWriter(
                    obtenerArchivo(ALUMNOS), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenciaException("Error al obtener el archivo");
        }
    }

    public BufferedReader getArchivoRegistros(int cedula) throws PersistenciaException {
        try {
            return abrirLectura(String.format(REGISTROS, String.valueOf(cedula)));
        } catch (IOException e) {
            throw new PersistenciaException("Error al obtener el archivo");
        }
    }

    public BufferedWriter getArchivoRegistrosEscritura(int cedula) throws PersistenciaException {
        if (!modifica) {
            throw new PersistenciaException("No se permite escribir el archivo");
        }

        try {
            return Files.newBufferedWriter(
                    obtenerArchivo(String.format(REGISTROS, String.valueOf(cedula))),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new PersistenciaException("Error al obtener el archivo");
        }
    }

    void finalize(boolean ok) throws IOException {
        if (ok) {
            confirmarCambios();
        } else {
            descartarCambios();
        }
    }

    private BufferedReader abrirLectura(String nombre) throws IOException {
        Path original = directorio.resolve(nombre);
        Path temporal = temporales.get(original);

        if (temporal != null) {
            return Files.newBufferedReader(temporal);
        }

        if (!Files.exists(original)) {
            return new BufferedReader(new StringReader(""));
        }

        return Files.newBufferedReader(original);
    }

    private Path obtenerArchivo(String nombre) throws PersistenciaException {
        Path original = directorio.resolve(nombre);

        if (!modifica) {
            return original;
        }

        Path temporal = temporales.get(original);

        if (temporal == null) {
            temporal = crearTemporal(original);
            temporales.put(original, temporal);
        }

        return temporal;
    }

    private Path crearTemporal(Path original) throws PersistenciaException {
        Path temporal = null;

        try {
            String nombre = original.getFileName().toString();

            temporal = Files.createTempFile(directorio, nombre + "-", ".temp");

            if (Files.exists(original)) {
                Files.copy(original, temporal, StandardCopyOption.REPLACE_EXISTING);
            }

            return temporal;
        } catch (IOException e) {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignored) {
                }
            }

            throw new PersistenciaException("Error al obtener el archivo");
        }
    }

    private void confirmarCambios() throws IOException {
        for (var entrada : temporales.entrySet()) {
            Path original = entrada.getKey();
            Path temporal = entrada.getValue();

            Files.move(temporal, original, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }

        temporales.clear();
    }

    private void descartarCambios() throws IOException {
        for (Path temporal : temporales.values()) {
            Files.deleteIfExists(temporal);
        }

        temporales.clear();
    }
}
