package com.picpay.rh;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FuncionarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExecutarFluxoCompletoDaApi() throws Exception {
        mockMvc.perform(get("/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(12)));

        String cadastro = """
                {
                  "nome": "Candidato Integração",
                  "email": "integracao@teste.com",
                  "telefone": "(11) 98888-0000",
                  "cargo": "Desenvolvedor Java",
                  "departamento": "Tecnologia",
                  "salario": 7500,
                  "cidade": "São Paulo"
                }
                """;

        mockMvc.perform(post("/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(13))
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        mockMvc.perform(get("/funcionarios/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integracao@teste.com"));

        String putCompleto = """
                {
                  "nome": "Candidato Integração Editado",
                  "email": "integracao.editado@teste.com",
                  "telefone": null,
                  "cargo": "Tech Lead",
                  "departamento": "Engenharia",
                  "salario": 9800,
                  "cidade": "Campinas",
                  "status": "EM_ANALISE"
                }
                """;

        mockMvc.perform(put("/funcionarios/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putCompleto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(13))
                .andExpect(jsonPath("$.nome").value("Candidato Integração Editado"))
                .andExpect(jsonPath("$.cargo").value("Tech Lead"));

        mockMvc.perform(patch("/funcionarios/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.nome").value("Candidato Integração Editado"));

        mockMvc.perform(get("/funcionarios/pesquisar").param("termo", "Tech Lead"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(13));

        mockMvc.perform(patch("/funcionarios/13")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INVALIDO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("Status inválido")));

        mockMvc.perform(delete("/funcionarios/13"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/funcionarios/13"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
