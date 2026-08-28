package com.picpay.rh.controller;

import com.picpay.rh.exception.ApiErro;
import com.picpay.rh.exception.FuncionarioNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(FuncionarioNaoEncontradoException.class)
    public ResponseEntity<ApiErro> naoEncontrado(
            FuncionarioNaoEncontradoException exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiErro> requisicaoInvalida(
            Exception exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErro> jsonInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        String mensagem = "JSON inválido ou valor incompatível com o campo informado.";
        if (exception.getMessage() != null && exception.getMessage().contains("StatusFuncionario")) {
            mensagem = "Status inválido. Use EM_ANALISE, APROVADO, REPROVADO ou CONTRATADO.";
        }
        return resposta(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErro> tipoInvalido(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "O ID informado deve ser um número inteiro válido.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErro> erroInterno(Exception exception, HttpServletRequest request) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado. Tente novamente.", request);
    }

    private ResponseEntity<ApiErro> resposta(HttpStatus status, String mensagem, HttpServletRequest request) {
        ApiErro erro = new ApiErro(
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                Instant.now());
        return ResponseEntity.status(status).body(erro);
    }
}
