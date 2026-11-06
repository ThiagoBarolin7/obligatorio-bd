package ude.obligatorio.persistencia.poolConexiones;

import ude.obligatorio.logica.excepciones.PersistenciaException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.lang.InterruptedException;

public class PoolConexiones implements IPoolConexiones {

    private String driver, url, user, password;    
    private int nivelTransaccionalidad;
    private IConexion[] conexiones;
    private int tamanio, creadas, tope;

    public PoolConexiones() throws PersistenciaException {
        try {
            Properties p = new Properties();

            try (InputStream is = PoolConexiones.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties")) {

                if (is == null) {
                    throw new IOException("No se encontró config.properties");
                }

                p.load(is);
            }

            driver = p.getProperty("driver");
            url = p.getProperty("url");
            user = p.getProperty("user");
            password = p.getProperty("password");
            tamanio = Integer.parseInt(p.getProperty("tamanio"));

            nivelTransaccionalidad = Connection.TRANSACTION_SERIALIZABLE;

            Class.forName(driver);
            conexiones = new IConexion[tamanio];
            creadas = 0;
            tope = 0;

        } catch (Exception e) {
            throw new PersistenciaException("Error al inicializar el pool de conexiones");
        }
    }

    @Override 
    public synchronized IConexion obtenerConexion(boolean modifica) throws PersistenciaException {
        try {
            while(tope == 0 && creadas == tamanio) {
                wait();
            }
        
            Conexion con;
            if (tope > 0) {
                tope--;
                con = (Conexion) conexiones[tope];
            } else {
                Connection c = DriverManager.getConnection(url, user, password);
                
                c.setTransactionIsolation(nivelTransaccionalidad);

                c.setAutoCommit(false);

                con = new Conexion(c);
                creadas++;
            }

            return con;
        } catch(SQLException | InterruptedException e) {
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
