package com.picpay.rh.service;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.entity.StatusFuncionario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioService {

    private final ArrayList<FuncionarioEntity> funcionarios = new ArrayList<>();

    private Integer proximoId = 0;

    public FuncionarioEntity cadastrar(FuncionarioEntity funcionario) {

        if (funcionario.getNome() == null || funcionario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (funcionario.getEmail() == null || funcionario.getEmail().isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }

        if (funcionario.getCargo() == null || funcionario.getCargo().isBlank()) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

        funcionario.setId(Integer.valueOf((int) proximoId++));

        if (funcionario.getStatus() == null) {
            funcionario.setStatus(StatusFuncionario.EM_ANALISE);
        }

        funcionarios.add(funcionario);

        return funcionario;
    }

    public List<FuncionarioEntity> listarTodos() {
        return funcionarios;
    }

    public FuncionarioEntity buscarPorId(Long id) {

        return funcionarios.stream()
                .filter(funcionario -> funcionario.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado."));
    }

    public FuncionarioEntity atualizar(Long id, FuncionarioEntity dadosAtualizados) {

        FuncionarioEntity funcionario = buscarPorId(id);

        if (dadosAtualizados.getNome() == null ||
                dadosAtualizados.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }

        if (dadosAtualizados.getEmail() == null ||
                dadosAtualizados.getEmail().isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }

        if (dadosAtualizados.getCargo() == null ||
                dadosAtualizados.getCargo().isBlank()) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }

        funcionario.setNome(dadosAtualizados.getNome());
        funcionario.setEmail(dadosAtualizados.getEmail());
        funcionario.setTelefone(dadosAtualizados.getTelefone());
        funcionario.setCargo(dadosAtualizados.getCargo());
        funcionario.setDepartamento(dadosAtualizados.getDepartamento());
        funcionario.setSalario(dadosAtualizados.getSalario());
        funcionario.setCidade(dadosAtualizados.getCidade());
        funcionario.setStatus(dadosAtualizados.getStatus());

        return funcionario;
    }

    public FuncionarioEntity atualizarParcial(Long id, FuncionarioEntity dados) {

        FuncionarioEntity funcionario = buscarPorId(id);

        if (dados.getNome() != null) {
            funcionario.setNome(dados.getNome());
        }

        if (dados.getEmail() != null) {
            funcionario.setEmail(dados.getEmail());
        }

        if (dados.getTelefone() != null) {
            funcionario.setTelefone(dados.getTelefone());
        }

        if (dados.getCargo() != null) {
            funcionario.setCargo(dados.getCargo());
        }

        if (dados.getDepartamento() != null) {
            funcionario.setDepartamento(dados.getDepartamento());
        }

        if (dados.getSalario() != null) {
            funcionario.setSalario(dados.getSalario());
        }

        if (dados.getCidade() != null) {
            funcionario.setCidade(dados.getCidade());
        }

        if (dados.getStatus() != null) {
            funcionario.setStatus(dados.getStatus());
        }

        return funcionario;
    }

    public void excluir(Long id) {

        FuncionarioEntity funcionario = buscarPorId(id);

        funcionarios.remove(funcionario);
    }

    public List<FuncionarioEntity> pesquisar(String termo) {

        if (termo == null || termo.isBlank()) {
            return funcionarios;
        }

        String pesquisa = termo.toLowerCase();

        return funcionarios.stream()
                .filter(funcionario ->

                funcionario.getNome()
                        .toLowerCase()
                        .contains(pesquisa)

                        ||

                        funcionario.getCargo()
                                .toLowerCase()
                                .contains(pesquisa)

                        ||

                        funcionario.getStatus()
                                .nome()
                                .toLowerCase()
                                .contains(pesquisa)

                )
                .toList();
    }
}