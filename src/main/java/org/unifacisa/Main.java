package org.unifacisa;

import javax.swing.JOptionPane;
import java.sql.*;

public class Main {
    private static Connection conexaoAtual = null;
    private static String perfilAtual = "";
    private static int idUsuarioLogadoSimulado = 2;

    public static void main(String[] args) {
        configurarPerfilAcesso();

        while (true) {
            String menu = "=== Sistema LibriTech (" + perfilAtual + ") ===\n";

            if (perfilAtual.equals("GERENTE")) {
                // CORREÇÃO: "Cadastrar Usuário" removido e numeração ajustada
                menu += "1- Listar Acervo\n2- Novo Livro\n3- Registrar Empréstimo\n4- Excluir Livro\n5- Processar Multas\n6- Sair";
            } else if (perfilAtual.equals("BIBLIOTECARIO")) {
                menu += "1- Listar Acervo\n3- Registrar Empréstimo\n5- Processar Multas\n6- Sair"; // Ajustado para 6 o Sair
            } else {
                menu += "1- Listar Acervo\n6- Sair"; // Ajustado para 6 o Sair
            }

            String opcao = JOptionPane.showInputDialog(menu);
            if (opcao == null || opcao.equals("6")) break; // Sair agora é 6 para unificar

            if ((perfilAtual.equals("ALUNO") || perfilAtual.equals("ESTAGIARIO")) && !opcao.equals("1")) {
                JOptionPane.showMessageDialog(null, "Acesso negado para esta função!");
                continue;
            }

            // CORREÇÃO: Switch reorganizado para refletir a remoção da opção antiga
            switch (opcao) {
                case "1": listarLivros(); break;
                case "2": inserirLivro(); break;
                case "3": registrarEmprestimo(); break;
                case "4": deletarLivro(); break;
                case "5": testarCalculoMulta(); break; // Antiga opção 6 virou 5
                default: JOptionPane.showMessageDialog(null, "Opção inválida!");
            }
        }
    }

    private static void configurarPerfilAcesso() {
        String[] perfis = {"GERENTE", "BIBLIOTECARIO", "ESTAGIARIO", "ALUNO"};
        String selecao = (String) JOptionPane.showInputDialog(null, "Selecione o perfil:", "Login",
                JOptionPane.QUESTION_MESSAGE, null, perfis, perfis[0]);

        if (selecao == null) System.exit(0);

        javax.swing.JPasswordField jpf = new javax.swing.JPasswordField();
        int result = JOptionPane.showConfirmDialog(null, jpf, "Senha para " + selecao, JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) System.exit(0);

        try {
            String usuarioBanco = "usr_" + selecao.toLowerCase();
            String senhaBanco = new String(jpf.getPassword());

            conexaoAtual = ConexaoBD.getCustomConnection(usuarioBanco, senhaBanco);
            perfilAtual = selecao;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Acesso Negado! Verifique usuário/senha no banco.\nErro: " + e.getMessage());
            System.exit(0);
        }
    }

    private static void listarLivros() {
        try {
            Statement stmt = conexaoAtual.createStatement();
            ResultSet rs;

            if (perfilAtual.equals("ALUNO")) {
                rs = stmt.executeQuery("SELECT id_livro, titulo, autor, isbn, quantidade_estoque FROM vw_acervo_publico");
            } else {
                rs = stmt.executeQuery("SELECT id_livro, titulo, autor, isbn, quantidade_estoque, preco_custo FROM Livros");
            }

            StringBuilder sb = new StringBuilder("--- ACERVO DA BIBLIOTECA ---\n");
            sb.append("ID | TÍTULO | AUTOR | ISBN\n---------------------------\n");

            while (rs.next()) {
                sb.append("[").append(rs.getInt("id_livro")).append("] ")
                        .append(rs.getString("titulo")).append(" - ")
                        .append(rs.getString("autor")).append(" | ISBN: ")
                        .append(rs.getString("isbn"));

                if (!perfilAtual.equals("ALUNO")) {
                    sb.append(" | R$ ").append(rs.getDouble("preco_custo"));
                }

                sb.append(" | Estq: ").append(rs.getInt("quantidade_estoque")).append("\n");
            }
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar acervo: " + e.getMessage());
        }
    }

    private static void inserirLivro() {
        try {
            String t = JOptionPane.showInputDialog("Título do Livro:");
            String a = JOptionPane.showInputDialog("Autor:");
            String i = JOptionPane.showInputDialog("ISBN:");
            String p = JOptionPane.showInputDialog("Preço de Custo (Ex: 25.50):");

            PreparedStatement pst = conexaoAtual.prepareStatement(
                    "INSERT INTO Livros (titulo, autor, isbn, quantidade_estoque, preco_custo) VALUES (?, ?, ?, 1, ?)"
            );
            pst.setString(1, t);
            pst.setString(2, a);
            pst.setString(3, i);
            pst.setDouble(4, Double.parseDouble(p));
            pst.executeUpdate();

            JOptionPane.showMessageDialog(null, "Livro cadastrado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar: " + e.getMessage());
        }
    }

    private static void deletarLivro() {
        try {
            String id = JOptionPane.showInputDialog("Digite o ID do livro que deseja EXCLUIR:");
            if (id == null || id.isEmpty()) return;

            PreparedStatement pst = conexaoAtual.prepareStatement("DELETE FROM Livros WHERE id_livro = ?");
            pst.setInt(1, Integer.parseInt(id));

            int linhasAfetadas = pst.executeUpdate();

            if (linhasAfetadas > 0) {
                JOptionPane.showMessageDialog(null, "Livro excluído com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Nenhum livro encontrado com esse ID.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erro: O ID deve ser um número.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "ERRO NO BANCO:\n" + e.getMessage());
        }
    }

    private static void registrarEmprestimo() {
        try {
            String idUsuario = JOptionPane.showInputDialog("ID do Usuário (Quem está pegando emprestado):");
            if (idUsuario == null || idUsuario.isEmpty()) return;

            String idLivro = JOptionPane.showInputDialog("ID do Livro:");
            if (idLivro == null || idLivro.isEmpty()) return;

            CallableStatement cstmt = conexaoAtual.prepareCall("{CALL sp_RegistrarEmprestimo(?, ?, 7)}");

            cstmt.setInt(1, Integer.parseInt(idUsuario));
            cstmt.setInt(2, Integer.parseInt(idLivro));

            cstmt.execute();
            JOptionPane.showMessageDialog(null, "Empréstimo realizado com sucesso!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erro: Os IDs devem ser números válidos.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "ALERTA DO BANCO (Trigger/Procedure):\n" + e.getMessage());
        }
    }

    private static void testarCalculoMulta() {
        try {
            String id = JOptionPane.showInputDialog("ID Empréstimo:");
            CallableStatement cstmt = conexaoAtual.prepareCall("{CALL sp_calcular_multa(?)}");
            cstmt.setInt(1, Integer.parseInt(id));
            cstmt.execute();
            JOptionPane.showMessageDialog(null, "Processamento de multa concluído.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao calcular multa: " + e.getMessage());
        }
    }
}