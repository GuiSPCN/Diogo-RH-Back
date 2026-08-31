package com.picpay.rh.exception;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String email) {
        super("Já existe um funcionário cadastrado com o e-mail " + email + ".");
    }
}
