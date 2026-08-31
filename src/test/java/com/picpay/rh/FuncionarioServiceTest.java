package com.picpay.rh;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.entity.StatusFuncionario;
import com.picpay.rh.exception.EmailDuplicadoException;
import com.picpay.rh.exception.FuncionarioNaoEncontradoException;
import com.picpay.rh.exception.RegraNegocioException;
import com.picpay.rh.service.FuncionarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuncionarioServiceTest {

    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        service = new FuncionarioService();
    }

    @Test
    void deveCarregarDadosDeDemonstracaoAoIniciar() {
        assertEquals(12, service.listarTodos().size());
        assertEquals(4, service.listarTodos().stream().filter(f -> f.getStatus() == StatusFuncionario.EM_ANALISE).count());
        assertEquals(3, service.listarTodos().stream().filter(f -> f.getStatus() == StatusFuncionario.APROVADO).count());
        assertEquals(2, service.listarTodos().stream().filter(f -> f.getStatus() == StatusFuncionario.REPROVADO).count());
        assertEquals(3, service.listarTodos().stream().filter(f -> f.getStatus() == StatusFuncionario.CONTRATADO).count());
    }

    @Test
    void deveCadastrarComIdUnicoStatusPadraoENormalizacao() {
        FuncionarioEntity criado = service.cadastrar(novo("  Maria Teste  ", "  MARIA@EXEMPLO.COM  "));

        assertNotNull(criado.getId());
        assertEquals("Maria Teste", criado.getNome());
        assertEquals("maria@exemplo.com", criado.getEmail());
        assertEquals(StatusFuncionario.EM_ANALISE, criado.getStatus());
    }

    @Test
    void deveRejeitarPostInvalido() {
        FuncionarioEntity invalido = novo("   ", "pessoa@exemplo.com");
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(invalido));
    }

    @Test
    void deveRejeitarEmailInvalidoESalarioNegativo() {
        FuncionarioEntity emailRuim = novo("Pessoa", "sem-arroba");
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(emailRuim));

        FuncionarioEntity salarioRuim = novo("Pessoa", "outra@exemplo.com");
        salarioRuim.setSalario(-1.0);
        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(salarioRuim));
    }

    @Test
    void deveRejeitarEmailDuplicadoIgnorandoMaiusculasEEspacos() {
        service.cadastrar(novo("Primeira", "duplicado@exemplo.com"));
        assertThrows(EmailDuplicadoException.class,
                () -> service.cadastrar(novo("Segunda", " DUPLICADO@EXEMPLO.COM ")));
    }

    @Test
    void deveBuscarPorIdERejeitarIdInexistente() {
        FuncionarioEntity criado = service.cadastrar(novo("Busca", "busca@exemplo.com"));
        assertEquals(criado.getId(), service.buscarPorId(criado.getId()).getId());
        assertThrows(FuncionarioNaoEncontradoException.class, () -> service.buscarPorId(99999));
    }

    @Test
    void putDeveAtualizarRecursoCompletoPreservandoId() {
        FuncionarioEntity criado = service.cadastrar(novo("Antes", "antes@exemplo.com"));
        Integer id = criado.getId();

        FuncionarioEntity atualizado = novo("Depois", "depois@exemplo.com");
        atualizado.setTelefone(null);
        atualizado.setDepartamento("Produto");
        atualizado.setSalario(8500.0);
        atualizado.setCidade("Recife");
        atualizado.setStatus(StatusFuncionario.APROVADO);

        FuncionarioEntity resposta = service.atualizar(id, atualizado);

        assertEquals(id, resposta.getId());
        assertEquals("Depois", resposta.getNome());
        assertEquals("Produto", resposta.getDepartamento());
        assertEquals(StatusFuncionario.APROVADO, resposta.getStatus());
    }

    @Test
    void putDeveRejeitarStatusAusenteEEmailDuplicado() {
        FuncionarioEntity criado = service.cadastrar(novo("Editável", "editavel@exemplo.com"));
        FuncionarioEntity semStatus = novo("Editável", "editavel2@exemplo.com");
        semStatus.setStatus(null);
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(criado.getId(), semStatus));

        FuncionarioEntity duplicado = novo("Editável", "ana.souza@rhflow.dev");
        duplicado.setStatus(StatusFuncionario.EM_ANALISE);
        assertThrows(EmailDuplicadoException.class, () -> service.atualizar(criado.getId(), duplicado));
    }

    @Test
    void patchDeveAlterarSomenteCampoEnviado() {
        FuncionarioEntity criado = service.cadastrar(novo("Original", "original@exemplo.com"));
        String emailAntes = criado.getEmail();
        String cargoAntes = criado.getCargo();

        FuncionarioEntity resposta = service.atualizarParcial(criado.getId(), Map.of("nome", "Alterado"));

        assertEquals("Alterado", resposta.getNome());
        assertEquals(emailAntes, resposta.getEmail());
        assertEquals(cargoAntes, resposta.getCargo());
    }

    @Test
    void patchDevePermitirLimparCamposOpcionais() {
        FuncionarioEntity criado = service.cadastrar(novo("Opcional", "opcional@exemplo.com"));
        criado.setTelefone("9999");

        FuncionarioEntity resposta = service.atualizarParcial(criado.getId(), java.util.Collections.singletonMap("telefone", null));

        assertEquals(null, resposta.getTelefone());
    }

    @Test
    void patchVazioCampoDesconhecidoEStatusInvalidoDevemFalharSemMutacaoParcial() {
        FuncionarioEntity criado = service.cadastrar(novo("Seguro", "seguro@exemplo.com"));
        assertThrows(IllegalArgumentException.class, () -> service.atualizarParcial(criado.getId(), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> service.atualizarParcial(criado.getId(), Map.of("batata", "x")));
        assertThrows(IllegalArgumentException.class, () -> service.atualizarParcial(criado.getId(), Map.of("status", "BATATA")));

        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarParcial(criado.getId(), Map.of("nome", "Mudaria", "salario", -10)));
        assertEquals("Seguro", service.buscarPorId(criado.getId()).getNome());
    }

    @Test
    void deveAplicarFluxoDeStatusEImpedirAtalhoParaContratado() {
        FuncionarioEntity criado = service.cadastrar(novo("Fluxo", "fluxo@exemplo.com"));
        assertThrows(RegraNegocioException.class,
                () -> service.atualizarParcial(criado.getId(), Map.of("status", "CONTRATADO")));

        service.atualizarParcial(criado.getId(), Map.of("status", "APROVADO"));
        assertEquals(StatusFuncionario.APROVADO, criado.getStatus());
    }

    @Test
    void contratadoExigeDepartamentoESalarioPositivo() {
        FuncionarioEntity criado = service.cadastrar(novo("Pronto", "pronto@exemplo.com"));
        service.atualizarParcial(criado.getId(), Map.of("status", "APROVADO"));
        service.atualizarParcial(criado.getId(), java.util.Collections.singletonMap("departamento", null));

        assertThrows(RegraNegocioException.class,
                () -> service.atualizarParcial(criado.getId(), Map.of("status", "CONTRATADO")));

        service.atualizarParcial(criado.getId(), Map.of("departamento", "Tecnologia", "salario", 9000));
        service.atualizarParcial(criado.getId(), Map.of("status", "CONTRATADO"));
        assertEquals(StatusFuncionario.CONTRATADO, criado.getStatus());
    }

    @Test
    void reprovadoPodeSerReabertoEContratadoFicaFinalizado() {
        FuncionarioEntity criado = service.cadastrar(novo("Reabrir", "reabrir@exemplo.com"));
        service.atualizarParcial(criado.getId(), Map.of("status", "REPROVADO"));
        service.atualizarParcial(criado.getId(), Map.of("status", "EM_ANALISE"));
        service.atualizarParcial(criado.getId(), Map.of("status", "APROVADO"));
        service.atualizarParcial(criado.getId(), Map.of("status", "CONTRATADO"));

        assertThrows(RegraNegocioException.class,
                () -> service.atualizarParcial(criado.getId(), Map.of("status", "EM_ANALISE")));
    }

    @Test
    void deleteDeveRemoverERejeitarIdInexistente() {
        FuncionarioEntity criado = service.cadastrar(novo("Excluir", "excluir@exemplo.com"));
        service.excluir(criado.getId());
        assertThrows(FuncionarioNaoEncontradoException.class, () -> service.buscarPorId(criado.getId()));
        assertThrows(FuncionarioNaoEncontradoException.class, () -> service.excluir(99999));
    }

    @Test
    void pesquisaDeveEncontrarNomeCargoEStatus() {
        assertFalse(service.pesquisar("Ana Souza").isEmpty());
        assertFalse(service.pesquisar("Tech Lead").isEmpty());
        assertFalse(service.pesquisar("em analise").isEmpty());
        assertTrue(service.pesquisar("termo-impossivel-xyz").isEmpty());
    }

    private FuncionarioEntity novo(String nome, String email) {
        return new FuncionarioEntity(
                null,
                nome,
                email,
                "(11) 99999-9999",
                "Desenvolvedor",
                "Tecnologia",
                5000.0,
                "São Paulo",
                null);
    }
}
