package sistemafarmacia.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String DB_NAME = "farmacia_db";
    private static final String USER = "postgres";
    private static final String PASS = "11111";

    private static final String URL = "jdbc:postgresql://localhost:5432/" + DB_NAME;

    private static Connection connection = null;

    public static Connection getInstance() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("Conexión a PostgreSQL se realizo correctamente");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error al conectar con la Base de Datos: " + e.getMessage());
        }
        return connection;
    }

    // Método para cerrar la conexión cuando cierres la app
    public static void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}