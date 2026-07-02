package org.unifacisa;

public class Aluno extends Usuario {
    public Aluno(String nome, String cpf, String senha) { 
        super(nome, cpf, senha); 
    }
    @Override
    public int getDiasPrazoEmprestimo() { 
        return 7; 
    } 
}