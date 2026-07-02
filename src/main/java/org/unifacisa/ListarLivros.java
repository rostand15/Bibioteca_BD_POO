package org.unifacisa;

import java.sql.*;

public class ListarLivros {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/db_libritech";
        String usuario = "usr_gerente";
        String senha = "123";

        try {
            Connection conexao = DriverManager.getConnection(url, usuario, senha);
            Statement comando = conexao.createStatement();
            

            ResultSet resultado = comando.executeQuery("SELECT * FROM Livros");

            System.out.println("--- ACERVO DA BIBLIOTECA ---");
            while (resultado.next()) {
                // Pegamos cada coluna pelo nome que definimos no banco
                int id = resultado.getInt("id_livro");
                String titulo = resultado.getString("titulo");
                String autor = resultado.getString("autor");
                String isbn = resultado.getString("isbn");
                double preco = resultado.getDouble("preco_custo");
                int estoque = resultado.getInt("quantidade_estoque");

                System.out.println("ID: " + id + " | Título: " + titulo + 
                                   " | Autor: " + autor + " | ISBN: " + isbn + 
                                   " | Preço: R$" + preco + " | Estoque: " + estoque);
            }

            conexao.close();
        } catch (SQLException e) {
            System.out.println("Erro ao listar livros.");
            e.printStackTrace();
        }
    }
}