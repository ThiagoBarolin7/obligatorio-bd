package ude.obligatorio.persistencia.poolConexiones;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import ude.obligatorio.logica.excepciones.PersistenciaException;

public class PoolConexionesTxt implements IPoolConexiones {

    private final Path directorio;
    private int numeroDeLectores;
    private boolean estaEscribiendo;

    public PoolConexionesTxt() throws PersistenciaException {
        try {
            Properties p = new Properties();
            p.load(new FileInputStream("config.properties"));
            directorio = Path.of(p.getProperty("directorio"));
        } catch (Exception e) {
            throw new PersistenciaException("Error al inicializar el pool de conexiones");
        }
    }

    @Override
    public synchronized IConexion obtenerConexion(boolean modifica) throws PersistenciaException {
        try {
            if (modifica) {
                adquirirEscritura();
            } else {
                adquirirLectura();
            }

            return new ConexionTxt(directorio, modifica);
        } catch (InterruptedException e) {
            throw new PersistenciaException("Error al obtener la conexion");
        }
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

    public void adquirirLectura() throws InterruptedException {
        while (estaEscribiendo) {
            this.wait();
        }
        numeroDeLectores++;
    }

    public void liberarLectura() {
        numeroDeLectores--;
        if (numeroDeLectores == 0) {
            this.notifyAll();
        }
    }

    public void adquirirEscritura() throws InterruptedException {
        while (estaEscribiendo || numeroDeLectores > 0) {
            this.wait();
        }
        estaEscribiendo = true;
    }

    public void liberarEscritura() {
        estaEscribiendo = false;
        this.notifyAll();
    }
}
