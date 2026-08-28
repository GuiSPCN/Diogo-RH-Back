package com.picpay.rh.controller;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.service.FuncionarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @PostMapping
    public ResponseEntity<FuncionarioEntity> cadastrar(@RequestBody FuncionarioEntity funcionario) {
        FuncionarioEntity criado = funcionarioService.cadastrar(funcionario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping
    public ResponseEntity<List<FuncionarioEntity>> listarTodos() {
        return ResponseEntity.ok(funcionarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(funcionarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> atualizar(
            @PathVariable Integer id,
            @RequestBody FuncionarioEntity funcionario) {
        return ResponseEntity.ok(funcionarioService.atualizar(id, funcionario));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FuncionarioEntity> atualizarParcial(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> alteracoes) {
        return ResponseEntity.ok(funcionarioService.atualizarParcial(id, alteracoes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        funcionarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<List<FuncionarioEntity>> pesquisar(
            @RequestParam(defaultValue = "") String termo) {
        return ResponseEntity.ok(funcionarioService.pesquisar(termo));
    }
}
