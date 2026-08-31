package com.picpay.rh.exception;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String mensagem,
        String path) {
}
