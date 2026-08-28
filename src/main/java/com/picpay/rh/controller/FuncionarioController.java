package com.picpay.rh.controller;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.service.FuncionarioService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/funcionarios")
@CrossOrigin(origins = "*")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    // Cadastrar funcionário
    @PostMapping
    public ResponseEntity<?> cadastrar(
            @RequestBody FuncionarioEntity funcionario) {

        try {

            FuncionarioEntity novoFuncionario = funcionarioService.cadastrar(funcionario);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(novoFuncionario);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Listar todos os funcionários
    @GetMapping
    public ResponseEntity<List<FuncionarioEntity>> listarTodos() {

        return ResponseEntity.ok(
                funcionarioService.listarTodos());
    }

    // Buscar funcionário por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Long id) {

        try {

            FuncionarioEntity funcionario = funcionarioService.buscarPorId(id);

            return ResponseEntity.ok(funcionario);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Atualização completa
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody FuncionarioEntity funcionario) {

        try {

            FuncionarioEntity atualizado = funcionarioService.atualizar(
                    id,
                    funcionario);

            return ResponseEntity.ok(atualizado);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Atualização parcial
    @PatchMapping("/{id}")
    public ResponseEntity<?> atualizarParcial(
            @PathVariable Long id,
            @RequestBody FuncionarioEntity funcionario) {

        try {

            FuncionarioEntity atualizado = funcionarioService.atualizarParcial(
                    id,
                    funcionario);

            return ResponseEntity.ok(atualizado);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Excluir funcionário
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @PathVariable Long id) {

        try {

            funcionarioService.excluir(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Pesquisar por nome, cargo ou status
    @GetMapping("/pesquisar")
    public ResponseEntity<List<FuncionarioEntity>> pesquisar(
            @RequestParam String termo) {

        return ResponseEntity.ok(
                funcionarioService.pesquisar(termo));
    }
}