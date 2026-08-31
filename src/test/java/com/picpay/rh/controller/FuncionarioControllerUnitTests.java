package com.picpay.rh.controller;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.entity.StatusFuncionario;
import com.picpay.rh.service.FuncionarioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FuncionarioControllerUnitTests {

    @Test
    void postRetorna201EDeleteRetorna204() {
        FuncionarioService service = new FuncionarioService();
        FuncionarioController controller = new FuncionarioController(service);

        FuncionarioEntity entrada = new FuncionarioEntity(
                null, "Teste Controller", "controller@exemplo.com", null,
                "Desenvolvedor", "Tecnologia", 5000.0, "Recife", StatusFuncionario.CONTRATADO);

        ResponseEntity<FuncionarioEntity> post = controller.cadastrar(entrada);
        assertEquals(HttpStatus.CREATED, post.getStatusCode());
        assertNotNull(post.getBody());
        // Mesmo que o cliente tente enviar outro status, o cadastro inicia em análise.
        assertEquals(StatusFuncionario.EM_ANALISE, post.getBody().getStatus());

        ResponseEntity<Void> delete = controller.excluir(post.getBody().getId());
        assertEquals(HttpStatus.NO_CONTENT, delete.getStatusCode());
    }

    @Test
    void patchPreservaCamposNaoEnviados() {
        FuncionarioService service = new FuncionarioService();
        FuncionarioController controller = new FuncionarioController(service);
        FuncionarioEntity entrada = new FuncionarioEntity(
                null, "Original", "original-controller@exemplo.com", "9999",
                "Analista", "Produto", 4500.0, "São Paulo", null);
        FuncionarioEntity criado = controller.cadastrar(entrada).getBody();

        ResponseEntity<FuncionarioEntity> patch = controller.atualizarParcial(
                criado.getId(), Map.of("status", "APROVADO"));

        assertEquals(HttpStatus.OK, patch.getStatusCode());
        assertEquals("Original", patch.getBody().getNome());
        assertEquals("original-controller@exemplo.com", patch.getBody().getEmail());
        assertEquals(StatusFuncionario.APROVADO, patch.getBody().getStatus());
    }
}
