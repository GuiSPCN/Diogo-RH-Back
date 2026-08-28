package com.picpay.rh;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.entity.StatusFuncionario;
import com.picpay.rh.exception.FuncionarioNaoEncontradoException;
import com.picpay.rh.service.FuncionarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FuncionarioServiceTest {

    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        service = new FuncionarioService();
    }

    @Test
    void deveCadastrarGerarIdEUsarStatusPadrao() {
        FuncionarioEntity criado = service.cadastrar(novo("Teste Cadastro", "cadastro@teste.com"));

        assertNotNull(criado.getId());
        assertEquals(13, criado.getId());
        assertEquals(StatusFuncionario.EM_ANALISE, criado.getStatus());
    }

    @Test
    void deveListarFuncionarios() {
        assertEquals(12, service.listarTodos().size());
    }

    @Test
    void deveEncontrarFuncionarioPorId() {
        FuncionarioEntity encontrado = service.buscarPorId(1);
        assertEquals(1, encontrado.getId());
        assertEquals("Marina Costa", encontrado.getNome());
    }

    @Test
    void deveRetornarErroParaIdInexistente() {
        assertThrows(FuncionarioNaoEncontradoException.class, () -> service.buscarPorId(9999));
    }

    @Test
    void putDeveAtualizarCompletamenteEPreservarId() {
        FuncionarioEntity dados = new FuncionarioEntity(
                999,
                "Marina Atualizada",
                "marina.atualizada@teste.com",
                null,
                "Tech Lead",
                "Engenharia",
                15000.0,
                null,
                StatusFuncionario.APROVADO);

        FuncionarioEntity atualizado = service.atualizar(1, dados);

        assertEquals(1, atualizado.getId());
        assertEquals("Marina Atualizada", atualizado.getNome());
        assertEquals("Tech Lead", atualizado.getCargo());
        assertEquals(StatusFuncionario.APROVADO, atualizado.getStatus());
        assertNull(atualizado.getTelefone());
        assertNull(atualizado.getCidade());
    }

    @Test
    void patchDeveAtualizarSomenteOCampoEnviado() {
        FuncionarioEntity antes = service.buscarPorId(1);
        FuncionarioEntity depois = service.atualizarParcial(1, Map.of("status", "APROVADO"));

        assertEquals(StatusFuncionario.APROVADO, depois.getStatus());
        assertEquals(antes.getNome(), depois.getNome());
        assertEquals(antes.getEmail(), depois.getEmail());
        assertEquals(antes.getCargo(), depois.getCargo());
    }

    @Test
    void patchDeveRecusarStatusInvalido() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizarParcial(1, Map.of("status", "QUALQUER_COISA")));

        assertTrue(erro.getMessage().contains("Status inválido"));
    }

    @Test
    void patchVazioDeveRetornarErro() {
        assertThrows(IllegalArgumentException.class, () -> service.atualizarParcial(1, Map.of()));
    }

    @Test
    void patchInvalidoNaoDeveAplicarAlteracoesParciais() {
        FuncionarioEntity antes = service.buscarPorId(1);
        Map<String, Object> alteracoes = new java.util.LinkedHashMap<>();
        alteracoes.put("nome", "Nome que não deve persistir");
        alteracoes.put("salario", -10);

        assertThrows(IllegalArgumentException.class, () -> service.atualizarParcial(1, alteracoes));

        FuncionarioEntity depois = service.buscarPorId(1);
        assertEquals(antes.getNome(), depois.getNome());
        assertEquals(antes.getSalario(), depois.getSalario());
    }

    @Test
    void pesquisaVaziaDeveRetornarTodosMesmoComCamposOpcionaisNulos() {
        assertEquals(12, service.pesquisar("   ").size());
        assertDoesNotThrow(() -> service.pesquisar("campinas"));
    }

    @Test
    void deleteDeveRemoverFuncionario() {
        service.excluir(1);
        assertThrows(FuncionarioNaoEncontradoException.class, () -> service.buscarPorId(1));
        assertEquals(11, service.listarTodos().size());
    }

    @Test
    void pesquisaDeveIgnorarMaiusculasEMinusculas() {
        var resultado = service.pesquisar("jAvA");

        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(f -> f.getCargo().toLowerCase().contains("java")));
    }

    @Test
    void salarioNegativoDeveSerRecusado() {
        FuncionarioEntity funcionario = novo("Salário Inválido", "salario@teste.com");
        funcionario.setSalario(-1.0);

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(funcionario));
    }

    @Test
    void camposObrigatoriosDevemSerValidados() {
        FuncionarioEntity funcionario = novo("   ", "obrigatorio@teste.com");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(funcionario));
    }

    @Test
    void emailInvalidoDeveSerRecusado() {
        FuncionarioEntity funcionario = novo("E-mail Inválido", "email-sem-arroba");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(funcionario));
    }

    @Test
    void emailDuplicadoDeveSerRecusado() {
        FuncionarioEntity funcionario = novo("Duplicado", "marina.costa@demo.com");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(funcionario));
    }

    @Test
    void putSemStatusDeveSerRecusado() {
        FuncionarioEntity funcionario = novo("PUT Incompleto", "put.incompleto@teste.com");

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(1, funcionario));
    }

    private FuncionarioEntity novo(String nome, String email) {
        return new FuncionarioEntity(
                null,
                nome,
                email,
                "(11) 90000-0000",
                "Desenvolvedor Java",
                "Tecnologia",
                7000.0,
                "São Paulo",
                null);
    }
}
