package ude.obligatorio.persistencia.poolConexiones;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import ude.obligatorio.logica.excepciones.PersistenciaException;

public class PoolConexionesTxt implements IPoolConexiones {

    private final Path directorio;
    private final int maximoEscritoresEnEspera;

    private int numeroDeLectores;
    private boolean estaEscribiendo;
    private int escritoresEsperando;

    public PoolConexionesTxt() throws PersistenciaException {
        try {
            Properties p = new Properties();

            try (InputStream is = PoolConexionesTxt.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties")) {

                if (is == null) {
                    throw new IOException("No se encontró config.properties");
                }

                p.load(is);
            }

            directorio = Path.of(p.getProperty("directorio"));

            Files.createDirectories(directorio);

            maximoEscritoresEnEspera = Integer.parseInt(p.getProperty("maximoEscritoresEnEspera"));

            estaEscribiendo = false;
            escritoresEsperando = 0;
        } catch (Exception e) {
            throw new PersistenciaException("Error al inicializar el pool de conexiones");
        }
    }

    @Override
    public synchronized IConexion obtenerConexion(boolean modifica) throws PersistenciaException {
        if (modifica) {
            adquirirEscritura();
        } else {
            adquirirLectura();
        }

        return new ConexionTxt(directorio, modifica);
    }

    @Override
    public synchronized void liberarConexion(IConexion icon, boolean ok) throws PersistenciaException {
        ConexionTxt con = (ConexionTxt) icon;

        if (con.modifica()) {
            try {
                con.finalize(ok);
            } catch (IOException e) {
                throw new PersistenciaException("Error al liberar la conexion");
            } finally {
                liberarEscritura();
            }
        } else {
            liberarLectura();
        }
    }

    public void adquirirLectura() throws PersistenciaException {
        try {
            while (estaEscribiendo || escritoresEsperando > maximoEscritoresEnEspera) {
                this.wait();
            }
        } catch (InterruptedException e) {
            throw new PersistenciaException("Error al obtener conexion");
        }

        numeroDeLectores++;
    }

    public void liberarLectura() {
        numeroDeLectores--;
        if (numeroDeLectores == 0) {
            this.notifyAll();
        }
    }

    public void adquirirEscritura() throws PersistenciaException {
        escritoresEsperando++;
        try {
            while (estaEscribiendo || numeroDeLectores > 0) {
                this.wait();
            }
        } catch (InterruptedException e) {
            throw new PersistenciaException("Error al obtener conexion");
        } finally {
            escritoresEsperando--;
        }

        estaEscribiendo = true;
    }

    public void liberarEscritura() {
        estaEscribiendo = false;
        this.notifyAll();
    }
}
