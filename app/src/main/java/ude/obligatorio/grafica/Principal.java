package ude.obligatorio.grafica;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ude.obligatorio.config.Config;
import ude.obligatorio.grafica.ventanas.VentanaPrincipal;
import ude.obligatorio.logica.IFachada;

/**
 * Punto de entrada de la capa grafica (equipo Usuario).
 * Busca la Fachada en el registro RMI del servidor central y abre el menu principal.
 */
public class Principal {

    public static void main(String[] args) {
        try {
            String host = Config.getObligatorio("rmi.host");
            int puerto = Integer.parseInt(Config.getObligatorio("rmi.puerto"));
            String nombre = Config.getObligatorio("rmi.nombre");

            Registry registry = LocateRegistry.getRegistry(host, puerto);
            final IFachada fachada = (IFachada) registry.lookup(nombre);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ignorada) {
                    }
                    new VentanaPrincipal(fachada).setVisible(true);
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo conectar con el servidor:\n" + e.getMessage()
                            + "\n\nVerifique que el servidor RMI este levantado.",
                    "Error de conexion",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
