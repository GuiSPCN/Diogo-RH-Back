package com.picpay.rh.exception;

public class FuncionarioNaoEncontradoException extends RuntimeException {
    public FuncionarioNaoEncontradoException(Integer id) {
        super("Funcionário com ID " + id + " não foi encontrado.");
    }
}
