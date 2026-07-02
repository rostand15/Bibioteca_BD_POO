package org.unifacisa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/db_libritech?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static Connection getCustomConnection(String usuario, String senha) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, usuario, senha);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado!", e);
        }
    }
}