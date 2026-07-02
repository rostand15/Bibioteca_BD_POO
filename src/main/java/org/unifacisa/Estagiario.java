package org.unifacisa;

public class Estagiario extends Usuario {
    public Estagiario(String nome, String cpf, String senha) { 
        super(nome, cpf, senha); 
    }
    @Override
    public int getDiasPrazoEmprestimo() { 
        return 10;
    } 
}