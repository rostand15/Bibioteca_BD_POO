package org.unifacisa;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;

public class LivroDAO {
    private Connection conn;

    public LivroDAO(Connection conn) {
        this.conn = conn;
    }
    public void listarLivros() {
        String sql = "SELECT * FROM Livros";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            StringBuilder sb = new StringBuilder("--- Acervo Completo ---\n");
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id_livro"))
                  .append(" | Título: ").append(rs.getString("titulo"))
                  .append(" | Estoque: ").append(rs.getInt("quantidade_estoque"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar: " + e.getMessage(), "Erro de Permissão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void listarLivrosViewPublica() {
        String sql = "SELECT * FROM vw_acervo_publico";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            StringBuilder sb = new StringBuilder("--- Acervo Público (Visão do Aluno) ---\n");
            while (rs.next()) {
                sb.append("Título: ").append(rs.getString("titulo"))
                  .append(" | Autor: ").append(rs.getString("autor"))
                  .append(" | Status: ").append(rs.getString("status"))
                  .append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao acessar View: " + e.getMessage(), "Erro de Permissão", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inserirLivro(String titulo, String autor, String isbn) {
        String sql = "INSERT INTO Livros (titulo, autor, isbn) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, titulo);
            stmt.setString(2, autor);
            stmt.setString(3, isbn);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Livro inserido com sucesso!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao inserir: " + e.getMessage(), "Erro MySQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deletarLivro(int id) {
        String sql = "DELETE FROM Livros WHERE id_livro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Livro deletado!");
            } else {
                JOptionPane.showMessageDialog(null, "ID não encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao deletar: " + e.getMessage(), "Erro MySQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void atualizarLivro(int id, String novoTitulo) {
        String sql = "UPDATE Livros SET titulo = ? WHERE id_livro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoTitulo);
            stmt.setInt(2, id);
            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                JOptionPane.showMessageDialog(null, "Livro atualizado!");
            } else {
                JOptionPane.showMessageDialog(null, "ID não encontrado.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e.getMessage(), "Erro MySQL", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void registrarEmprestimo(int idUsuario, int idLivro, int diasPrazo) {
        String sql = "{CALL sp_RegistrarEmprestimo(?, ?, ?)}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idLivro);
            
            LocalDate dataPrevista = LocalDate.now().plusDays(diasPrazo);
            stmt.setDate(3, Date.valueOf(dataPrevista));
            
            stmt.execute();
            JOptionPane.showMessageDialog(null, "Empréstimo efetuado com sucesso via Procedure!\nData prevista de devolução: " + dataPrevista);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao registrar empréstimo: " + e.getMessage(), "Erro de Validação/Trigger", JOptionPane.ERROR_MESSAGE);
        }
    }
}