package org.unifacisa;

public class Bibliotecario extends Usuario {

    public Bibliotecario(String nome, String cpf, String senha) { 
        super(nome, cpf, senha); 
    }

    @Override
    public int getDiasPrazoEmprestimo() { 
        return 14; 
    } 
}