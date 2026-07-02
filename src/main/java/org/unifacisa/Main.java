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
                menu += "1- Listar Acervo\n2- Novo Livro\n3- Registrar Empréstimo\n4- Excluir Livro\n5- Cadastrar Usuário\n6- Processar Multas\n7- Sair";
            } else if (perfilAtual.equals("BIBLIOTECARIO")) {
                menu += "1- Listar Acervo\n3- Registrar Empréstimo\n6- Processar Multas\n7- Sair";
            } else {
                menu += "1- Listar Acervo\n7- Sair";
            }

            String opcao = JOptionPane.showInputDialog(menu);
            if (opcao == null || opcao.equals("7")) break;

            if ((perfilAtual.equals("ALUNO") || perfilAtual.equals("ESTAGIARIO")) && !opcao.equals("1")) {
                JOptionPane.showMessageDialog(null, "Acesso negado para esta função!");
                continue;
            }

            switch (opcao) {
                case "1": listarLivros(); break;
                case "2": inserirLivro(); break;
                case "3": registrarEmprestimo(); break;
                case "4": deletarLivro(); break;
                case "5": testarTransacaoAtomica(); break;
                case "6": testarCalculoMulta(); break;
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
            // CORREÇÃO: Ajustado para bater com o padrão 'usr_nome' do seu script SQL
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

            // CORREÇÃO: Aluno consome a VIEW pública (sem preço de custo). Outros perfis consomem a tabela direta.
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

                // Exibe o preço de custo apenas para quem tem acesso à tabela base
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
            // Antes de registrar, precisamos garantir que o usuário simulado exista no banco por causa da FK.
            // Para testes rápidos, certifique-se de que a tabela 'Usuarios' possua um registro com ID = 2.
            String id = JOptionPane.showInputDialog("ID Livro:");
            CallableStatement cstmt = conexaoAtual.prepareCall("{CALL sp_RegistrarEmprestimo(?, ?, 7)}");
            cstmt.setInt(1, idUsuarioLogadoSimulado);
            cstmt.setInt(2, Integer.parseInt(id));
            cstmt.execute();
            JOptionPane.showMessageDialog(null, "Empréstimo realizado com sucesso!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "ALERTA DO BANCO (Trigger/Procedure):\n" + e.getMessage());
        }
    }

    private static void testarTransacaoAtomica() {
        try {
            // Passando 'SP' como UF para passar na validação da Procedure
            CallableStatement cstmt = conexaoAtual.prepareCall("{CALL sp_transacao_cadastro_completo(?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cstmt.setString(1, "Novo Usuario");
            cstmt.setString(2, "11122233344");
            cstmt.setString(3, "teste@libritech.com");
            cstmt.setString(4, "senha123");
            cstmt.setString(5, "ALUNO");
            cstmt.setString(6, "Rua das Flores, 123");
            cstmt.setString(7, "Centro");
            cstmt.setString(8, "São Paulo");
            cstmt.setString(9, "SP");
            cstmt.execute();
            JOptionPane.showMessageDialog(null, "Usuário e Endereço cadastrados via Transação!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "INTEGRIDADE MANTIDA:\n" + e.getMessage());
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