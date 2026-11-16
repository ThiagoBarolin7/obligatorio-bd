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

            nivelTransaccionalidad = nivelTransaccionalidadConfigurado();

            Class.forName(driver);
            conexiones = new IConexion[tamanio];
            creadas = 0;
            tope = 0;

        } catch (PersistenciaException e) {
            throw e;
        } catch (Exception e) {
            throw new PersistenciaException("Error al inicializar el pool de conexiones: " + e.getMessage());
        }
    }

    private static int nivelTransaccionalidadConfigurado() throws PersistenciaException {
        String valor = Config.getObligatorio("nivelTransaccionalidad").trim();

        switch (valor.toUpperCase()) {
            case "READ_UNCOMMITTED":
                return Connection.TRANSACTION_READ_UNCOMMITTED;
            case "READ_COMMITTED":
                return Connection.TRANSACTION_READ_COMMITTED;
            case "REPEATABLE_READ":
                return Connection.TRANSACTION_REPEATABLE_READ;
            case "SERIALIZABLE":
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                try {
                    int nivel = Integer.parseInt(valor);

                    if (nivel == Connection.TRANSACTION_READ_UNCOMMITTED
                            || nivel == Connection.TRANSACTION_READ_COMMITTED
                            || nivel == Connection.TRANSACTION_REPEATABLE_READ
                            || nivel == Connection.TRANSACTION_SERIALIZABLE) {
                        return nivel;
                    }
                } catch (NumberFormatException e) {
                }

                throw new PersistenciaException("Nivel de transaccionalidad invalido: '" + valor + "'");
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
                con = conexiones[tope];
            } else {
                Connection c = DriverManager.getConnection(url, user, password);

                try {
                    c.setTransactionIsolation(nivelTransaccionalidad);
                    c.setAutoCommit(false);
                } catch (SQLException e) {
                    try {
                        c.close();
                    } catch (SQLException ignorada) {
                    }
                    throw e;
                }

                con = new Conexion(c);
                creadas++;
            }

            try {
                ((Conexion) con).getConnection().setReadOnly(!modifica);
            } catch (SQLException e) {
                try {
                    ((Conexion) con).getConnection().close();
                } catch (SQLException ignorada) {
                }
                creadas--;
                notifyAll();
                throw e;
            }

            return con;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenciaException("Interrumpido al obtener conexion");
        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener conexion: " + e.getMessage());
        }
    }

    @Override
    public synchronized void liberarConexion(IConexion icon, boolean ok) throws PersistenciaException {
        Connection con = ((Conexion) icon).getConnection();
        boolean descartar = false;

        try {
            if (!con.getAutoCommit()) {
                if (ok) {
                    con.commit();
                } else {
                    con.rollback();
                }
            }
            con.setReadOnly(false);
        } catch (SQLException e) {
            descartar = true;
            throw new PersistenciaException("Error al liberar la conexion: " + e.getMessage());
        } finally {
            if (descartar) {
                try {
                    con.close();
                } catch (SQLException ignorada) {
                }
                creadas--;
            } else {
                conexiones[tope] = icon;
                tope++;
            }
            notifyAll();
        }
    }
}
