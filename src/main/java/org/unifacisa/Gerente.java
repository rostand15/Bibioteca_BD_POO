package org.unifacisa;

public class Gerente extends Usuario {
    public Gerente(String nome, String cpf, String senha) { 
        super(nome, cpf, senha); 
    }
    @Override
    public int getDiasPrazoEmprestimo() { 
        return 30; 
    } 
}