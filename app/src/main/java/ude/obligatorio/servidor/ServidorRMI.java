package ude.obligatorio.servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import ude.obligatorio.config.Config;
import ude.obligatorio.logica.Fachada;
import ude.obligatorio.logica.IFachada;

public class ServidorRMI {

    public static void main(String[] args) {
        try {
            int puerto = Integer.parseInt(Config.getObligatorio("rmi.puerto"));
            String nombre = Config.getObligatorio("rmi.nombre");
            int puertoServidor = Integer.parseInt(Config.getObligatorio("rmi.servidor.puerto"));

            IFachada fachada = new Fachada();

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(puerto);
                System.out.println("Registro RMI creado en el puerto " + puerto);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(puerto);
                System.out.println("Usando el registro RMI ya existente en el puerto " + puerto);
            }

            UnicastRemoteObject.exportObject(fachada, puertoServidor);
            registry.rebind(nombre, fachada);

            System.out.println("Fachada publicada como \"" + nombre + "\".");
            System.out.println("Servidor listo. Ctrl+C para detenerlo.");

        } catch (Exception e) {
            System.err.println("No se pudo levantar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
