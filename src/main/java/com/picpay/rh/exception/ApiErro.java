package com.picpay.rh.exception;

import java.time.Instant;

public record ApiErro(
        int status,
        String erro,
        String mensagem,
        String caminho,
        Instant timestamp) {
}
