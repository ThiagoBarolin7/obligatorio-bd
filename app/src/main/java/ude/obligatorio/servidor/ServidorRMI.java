package ude.obligatorio.servidor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import ude.obligatorio.logica.Fachada;
import ude.obligatorio.logica.IFachada;

/**
 * Servidor central: crea la Fachada y la publica en el registro RMI para que
 * los equipos clientes puedan invocarla.
 */
public class ServidorRMI {

    public static void main(String[] args) {
        try {
            Properties cfg = new Properties();

            try (InputStream is = ServidorRMI.class
                    .getClassLoader()
                    .getResourceAsStream("config.properties")) {

                if (is == null) {
                    throw new IOException("No se encontró config.properties en el classpath");
                }

                cfg.load(is);
            }

            int puerto = Integer.parseInt(cfg.getProperty("rmi.puerto", "1099"));
            String nombre = cfg.getProperty("rmi.nombre", "Fachada");

            IFachada fachada = new Fachada();

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(puerto);
                System.out.println("Registro RMI creado en el puerto " + puerto);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(puerto);
                System.out.println("Usando el registro RMI ya existente en el puerto " + puerto);
            }

            registry.rebind(nombre, fachada);

            System.out.println("Fachada publicada como \"" + nombre + "\".");
            System.out.println("Servidor listo. Ctrl+C para detenerlo.");

        } catch (Exception e) {
            System.err.println("No se pudo levantar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
