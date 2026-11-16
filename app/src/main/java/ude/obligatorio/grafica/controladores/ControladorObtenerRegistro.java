package ude.obligatorio.grafica.controladores;

import ude.obligatorio.grafica.ventanas.VentanaObtenerRegistro;
import ude.obligatorio.logica.IFachada;
import ude.obligatorio.logica.excepciones.NotasException;
import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.logica.valueObjects.VORegistro;

/** Controlador del requerimiento "obtener registro". */
public class ControladorObtenerRegistro {

    private IFachada fachada;
    private VentanaObtenerRegistro ventana;

    public ControladorObtenerRegistro(IFachada fachada, VentanaObtenerRegistro ventana) {
        this.fachada = fachada;
        this.ventana = ventana;
    }

    public void obtenerRegistro(String cedula, String numero) {
        int ced;
        int num;
        try {
            ced = Integer.parseInt(cedula.trim());
        } catch (NumberFormatException e) {
            ventana.mostrarError("La cedula debe ser un numero entero.");
            return;
        }
        try {
            num = Integer.parseInt(numero.trim());
        } catch (NumberFormatException e) {
            ventana.mostrarError("El numero de registro debe ser un numero entero.");
            return;
        }

        try {
            VORegistro voR = fachada.obtenerRegistro(ced, num);

            if (voR == null) {
                ventana.limpiarResultado();
                ventana.mostrarError("El alumno " + ced + " no tiene un registro con numero " + num);
            } else {
                ventana.mostrarRegistro(voR);
            }
        } catch (NotasException e) {
            ventana.limpiarResultado();
            ventana.mostrarError(e.getMessage());
        } catch (PersistenciaException e) {
            ventana.limpiarResultado();
            ventana.mostrarError("Error de persistencia: " + e.getMessage());
        } catch (java.rmi.RemoteException e) {
            ventana.limpiarResultado();
            ventana.mostrarError("Error de comunicacion con el servidor: " + e.getMessage());
        }
    }
}
