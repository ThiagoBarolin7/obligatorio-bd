package ude.obligatorio.persistencia.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import ude.obligatorio.logica.Alumno;
import ude.obligatorio.logica.excepciones.PersistenciaException;
import ude.obligatorio.logica.valueObjects.VOAlumnoProm;
import ude.obligatorio.persistencia.consultas.Consultas;
import ude.obligatorio.persistencia.poolConexiones.Conexion;
import ude.obligatorio.persistencia.poolConexiones.IConexion;

public class DAOAlumnos implements IDAOAlumnos {

    @Override
    public boolean member(IConexion icon, int ced) throws PersistenciaException {
        boolean existeCedula = false;
        Connection con = ((Conexion) icon).getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(new Consultas().existeAlumno()); ) {

            pstmt.setInt(1, ced);

            try (ResultSet rs = pstmt.executeQuery(); ) {
                existeCedula = rs.next();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de acceso a los datos (member alumno)");
        }

        return existeCedula;
    }

    @Override
    public void insert(IConexion icon, Alumno alumno) throws PersistenciaException {
        Connection con = ((Conexion) icon).getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(new Consultas().insertarAlumno()); ) {

            pstmt.setInt(1, alumno.getCedula());
            pstmt.setString(2, alumno.getNombre());
            pstmt.setString(3, alumno.getApellido());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Error de acceso a los datos (insert)");
        }
    }

    @Override
    public Alumno find(IConexion icon, int cedA) throws PersistenciaException {
        Alumno alu = null;
        Connection con = ((Conexion) icon).getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(new Consultas().obtenerAlumno()); ) {

            pstmt.setInt(1, cedA);

            try (ResultSet rs = pstmt.executeQuery(); ) {
                if (rs.next()) {
                    alu = new Alumno(rs.getInt("cedula"), rs.getString("nombre"), rs.getString("apellido"));
                }
            }

        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener alumno");
        }

        return alu;
    }

    @Override
    public boolean esVacio(IConexion icon) throws PersistenciaException {
        boolean vacio = true;
        Connection con = ((Conexion) icon).getConnection();

        try (PreparedStatement pstmt = con.prepareStatement(new Consultas().cantidadAlumnos());
                ResultSet rs = pstmt.executeQuery(); ) {

            if (rs.next()) vacio = rs.getInt(1) == 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Error de acceso a los datos (esVacio)");
        }

        return vacio;
    }

    @Override
    public List<Alumno> listarAlumnos(IConexion icon) throws PersistenciaException {
        List<Alumno> lista = new LinkedList<Alumno>();
        Connection con = ((Conexion) icon).getConnection();
        try (Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(new Consultas().listarAlumnos()); ) {

            while (rs.next()) {
                lista.add(new Alumno(rs.getInt("cedula"), rs.getString("nombre"), rs.getString("apellido")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de acceso a los datos (listarAlumno())");
        }

        return lista;
    }

    @Override
    public VOAlumnoProm alumnoMayorPromedio(IConexion icon) throws PersistenciaException {
        Alumno mejor = null;
        double mayor = 0;
        List<Alumno> alumnos = listarAlumnos(icon);
        Connection con = ((Conexion) icon).getConnection();
        try (PreparedStatement pstmt = con.prepareStatement(new Consultas().promedioNotas())) {

            for (Alumno a : alumnos) {
                double prom;
                pstmt.setInt(1, a.getCedula());

                try (ResultSet rs = pstmt.executeQuery()) {
                    prom = rs.next() ? rs.getDouble("promedio") : 0;
                }

                if (mejor == null || prom > mayor) {
                    mejor = a;
                    mayor = prom;
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al devolver el mayor promedio)");
        }

        if (mejor == null) return null;

        return new VOAlumnoProm(mejor.getCedula(), mejor.getNombre(), mejor.getApellido(), mayor);
    }

    @Override
    public int contarAlumnos(IConexion icon, String act, int nota) throws PersistenciaException {
        int cant = 0;
        Connection con = ((Conexion) icon).getConnection();
        try (PreparedStatement pstmt = con.prepareStatement(new Consultas().contarAlumnos()); ) {

            pstmt.setString(1, act);
            pstmt.setInt(2, nota);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) cant = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error de acceso a los datos (contar alumnos)");
        }
        return cant;
    }
}
