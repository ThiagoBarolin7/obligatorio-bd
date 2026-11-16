package ude.obligatorio.logica;

public class Registro {

    private String actividad;
    private int nota, numero;

    public Registro(int numero, int nota, String actividad) {
        this.numero = numero;
        this.nota = nota;
        this.actividad = actividad;
    }

    public String getActividad() {
        return actividad;
    }

    public int getNota() {
        return nota;
    }
    public int getNumero() {
        return numero;
    }
}
