package ude.obligatorio.persistencia.consultas;

/**
 * Textos de todas las sentencias SQL que la aplicacion necesita ejecutar.
 * No ejecuta nada: solo centraliza el SQL en un unico lugar.
 */
public class Consultas {

    public String existeAlumno() {
        return """
               SELECT cedula
               FROM Alumnos
               WHERE cedula = ?
               """;
    }

    public String obtenerAlumno() {
        return """
               SELECT cedula, nombre, apellido
               FROM Alumnos
               WHERE cedula = ?
               """;
    }

    public String insertarAlumno() {
        return """
               INSERT INTO Alumnos (cedula, nombre, apellido)
               VALUES (?, ?, ?)
               """;
    }

    public String listarAlumnos() {
        return """
               SELECT cedula, nombre, apellido
               FROM Alumnos
               ORDER BY cedula
               """;
    }

    public String cantidadAlumnos() {
        return """
               SELECT COUNT(*) AS cantidad
               FROM Alumnos
               """;
    }

    public String insertarRegistro() {
        return """
               INSERT INTO Registros (numero, cedAlu, actividad, nota)
               VALUES (?, ?, ?, ?)
               """;
    }

    public String obtenerRegistro() {
        return """
               SELECT numero, actividad, nota
               FROM Registros
               WHERE cedAlu = ?
                 AND numero = ?
               """;
    }

    public String cantidadRegistros() {
        return """
               SELECT COUNT(*) AS cantidad
               FROM Registros
               WHERE cedAlu = ?
               """;
    }

    public String listarRegistros() {
        return """
               SELECT numero, actividad, nota
               FROM Registros
               WHERE cedAlu = ?
               ORDER BY numero
               """;
    }

    public String promedioNotas() {
        return """
               SELECT AVG(nota) AS promedio
               FROM Registros
               WHERE cedAlu = ?
               """;
    }

    public String contarAlumnos() {
        return """
               SELECT COUNT(DISTINCT cedAlu) AS cantidad
               FROM Registros
               WHERE actividad = ?
                 AND nota = ?
               """;
    }

    public String actividadMasCursada() {
        return """
               SELECT actividad, COUNT(*) AS veces
               FROM Registros
               WHERE cedAlu = ?
               GROUP BY actividad
               ORDER BY veces DESC
               LIMIT 1
               """;
    }
}
