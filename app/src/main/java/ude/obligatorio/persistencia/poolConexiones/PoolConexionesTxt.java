package ude.obligatorio.persistencia.poolConexiones;

import java.nio.file.Files;
import java.nio.file.Path;
import ude.obligatorio.config.Config;
import ude.obligatorio.logica.excepciones.PersistenciaException;

public class PoolConexionesTxt implements IPoolConexiones {

    private final Path directorio;
    private final int maximoEscritoresEnEspera;

    private int numeroDeLectores;
    private boolean estaEscribiendo;
    private int escritoresEsperando;

    public PoolConexionesTxt() throws PersistenciaException {
        try {
            directorio = Path.of(Config.getObligatorio("directorio"));
            maximoEscritoresEnEspera = Math.max(0, Config.getInt("maximoEscritoresEnEspera", 2));

            Files.createDirectories(directorio);

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
            } catch (Exception e) {
                throw new PersistenciaException("Error al liberar la conexion");
            } finally {
                liberarEscritura();
            }
        } else {
            liberarLectura();
        }
    }

    private void adquirirLectura() throws PersistenciaException {
        try {
            while (estaEscribiendo || escritoresEsperando > maximoEscritoresEnEspera) {
                this.wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("Error al obtener conexion");
        }

        numeroDeLectores++;
    }

    private void liberarLectura() {
        numeroDeLectores--;
        if (numeroDeLectores == 0) {
            this.notifyAll();
        }
    }

    private void adquirirEscritura() throws PersistenciaException {
        escritoresEsperando++;
        try {
            while (estaEscribiendo || numeroDeLectores > 0) {
                this.wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("Error al obtener conexion");
        } finally {
            escritoresEsperando--;
            this.notifyAll();
        }

        estaEscribiendo = true;
    }

    private void liberarEscritura() {
        estaEscribiendo = false;
        this.notifyAll();
    }
}
