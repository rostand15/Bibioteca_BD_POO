package org.unifacisa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InserirLivro {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/db_libritech";
        String usuario = "usr_gerente";
        String senha = "123";

        // Dados do novo livro
        String titulo = "Java para Iniciantes";
        String autor = "Programador Java";
        String isbn = "978-0000000001";
        double preco = 59.90;
        int estoque = 10;
        String status = "DISPONIVEL";

        try {
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            
            String sql = "INSERT INTO Livros (titulo, autor, isbn, preco_custo, quantidade_estoque, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement comando = conexao.prepareStatement(sql);
            
            // Preenchendo as colunas na ordem correta
            comando.setString(1, titulo);
            comando.setString(2, autor);
            comando.setString(3, isbn);
            comando.setDouble(4, preco);
            comando.setInt(5, estoque);
            comando.setString(6, status);
            
            comando.executeUpdate();
            
            System.out.println("SUCESSO: Livro '" + titulo + "' cadastrado com sucesso!");
            
            conexao.close();
        } catch (SQLException e) {
            System.out.println("ERRO ao inserir livro:");
            e.printStackTrace();
        }
    }
}