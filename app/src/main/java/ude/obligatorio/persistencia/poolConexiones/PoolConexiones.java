package ude.obligatorio.persistencia.poolConexiones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import ude.obligatorio.config.Config;
import ude.obligatorio.logica.excepciones.PersistenciaException;

/**
 * Pool de conexiones JDBC para MySQL.
 * Lee configuración desde variables de entorno o config.properties.
 */
public class PoolConexiones implements IPoolConexiones {

    private String driver, url, user, password;
    private int nivelTransaccionalidad;
    private IConexion[] conexiones;
    private int tamanio, creadas, tope;

    public PoolConexiones() throws PersistenciaException {
        try {
            driver = Config.getObligatorio("driver");
            url = Config.getObligatorio("url");
            user = Config.getObligatorio("user");
            password = Config.getObligatorio("password");
            tamanio = Config.getInt("tamanio", 5);

            nivelTransaccionalidad = Connection.TRANSACTION_SERIALIZABLE;

            Class.forName(driver);
            conexiones = new IConexion[tamanio];
            creadas = 0;
            tope = 0;

        } catch (Exception e) {
            throw new PersistenciaException("Error al inicializar el pool de conexiones: " + e.getMessage());
        }
    }

    @Override
    public synchronized IConexion obtenerConexion(boolean modifica) throws PersistenciaException {
        try {
            while (tope == 0 && creadas == tamanio) {
                wait();
            }

            IConexion con;
            if (tope > 0) {
                tope--;
                con = (IConexion) conexiones[tope];
            } else {
                Connection c = DriverManager.getConnection(url, user, password);

                c.setTransactionIsolation(nivelTransaccionalidad);
                c.setAutoCommit(false);

                con = new Conexion(c);
                creadas++;
            }

            return con;
        } catch (SQLException | InterruptedException e) {
            throw new PersistenciaException("Error al obtener conexion");
        }
    }

    @Override
    public synchronized void liberarConexion(IConexion icon, boolean ok) throws PersistenciaException {
        try {
            Connection con = ((Conexion) icon).getConnection();

            if (!con.getAutoCommit()) {
                if (ok) {
                    con.commit();
                } else {
                    con.rollback();
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al liberar la conexion");
        } finally {
            conexiones[tope] = icon;
            tope++;
            notify();
        }
    }
}
