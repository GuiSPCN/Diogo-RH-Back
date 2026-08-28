package com.picpay.rh.service;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.entity.StatusFuncionario;
import com.picpay.rh.exception.FuncionarioNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FuncionarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Set<String> CAMPOS_PATCH = Set.of(
            "nome", "email", "telefone", "cargo", "departamento", "salario", "cidade", "status");

    private final ArrayList<FuncionarioEntity> funcionarios = new ArrayList<>();
    private Integer proximoId = 1;

    public FuncionarioService() {
        carregarDadosDemonstracao();
    }

    public synchronized FuncionarioEntity cadastrar(FuncionarioEntity funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("O corpo da requisição é obrigatório.");
        }

        FuncionarioEntity novo = copiar(funcionario);
        normalizarTextos(novo);

        if (novo.getStatus() == null) {
            novo.setStatus(StatusFuncionario.EM_ANALISE);
        }

        validarDados(novo, true);
        validarEmailDuplicado(novo.getEmail(), null);

        novo.setId(proximoId++);
        funcionarios.add(novo);
        return copiar(novo);
    }

    public synchronized List<FuncionarioEntity> listarTodos() {
        return funcionarios.stream().map(this::copiar).toList();
    }

    public synchronized FuncionarioEntity buscarPorId(Integer id) {
        return copiar(buscarEntidadePorId(id));
    }

    public synchronized FuncionarioEntity atualizar(Integer id, FuncionarioEntity dadosAtualizados) {
        if (dadosAtualizados == null) {
            throw new IllegalArgumentException("O corpo da requisição é obrigatório.");
        }

        FuncionarioEntity funcionario = buscarEntidadePorId(id);
        FuncionarioEntity dados = copiar(dadosAtualizados);
        normalizarTextos(dados);

        // PUT representa substituição completa dos dados editáveis. Status precisa ser informado.
        if (dados.getStatus() == null) {
            throw new IllegalArgumentException("O status é obrigatório em uma atualização completa (PUT).");
        }

        validarDados(dados, true);
        validarEmailDuplicado(dados.getEmail(), id);

        funcionario.setNome(dados.getNome());
        funcionario.setEmail(dados.getEmail());
        funcionario.setTelefone(dados.getTelefone());
        funcionario.setCargo(dados.getCargo());
        funcionario.setDepartamento(dados.getDepartamento());
        funcionario.setSalario(dados.getSalario());
        funcionario.setCidade(dados.getCidade());
        funcionario.setStatus(dados.getStatus());

        return copiar(funcionario);
    }

    public synchronized FuncionarioEntity atualizarParcial(Integer id, Map<String, Object> alteracoes) {
        FuncionarioEntity funcionario = buscarEntidadePorId(id);

        if (alteracoes == null || alteracoes.isEmpty()) {
            throw new IllegalArgumentException("O PATCH precisa informar pelo menos um campo para atualização.");
        }

        String campoDesconhecido = alteracoes.keySet().stream()
                .filter(campo -> !CAMPOS_PATCH.contains(campo))
                .findFirst()
                .orElse(null);
        if (campoDesconhecido != null) {
            throw new IllegalArgumentException("Campo não permitido no PATCH: " + campoDesconhecido + ".");
        }

        // Trabalha sobre uma cópia para que um PATCH inválido seja atômico:
        // se qualquer campo falhar na validação, o objeto armazenado permanece intacto.
        FuncionarioEntity dados = copiar(funcionario);

        if (alteracoes.containsKey("nome")) {
            dados.setNome(textoObrigatorio(alteracoes.get("nome"), "nome"));
        }

        if (alteracoes.containsKey("email")) {
            String email = textoObrigatorio(alteracoes.get("email"), "e-mail").toLowerCase(Locale.ROOT);
            validarEmail(email);
            validarEmailDuplicado(email, id);
            dados.setEmail(email);
        }

        if (alteracoes.containsKey("telefone")) {
            dados.setTelefone(textoOpcional(alteracoes.get("telefone")));
        }

        if (alteracoes.containsKey("cargo")) {
            dados.setCargo(textoObrigatorio(alteracoes.get("cargo"), "cargo"));
        }

        if (alteracoes.containsKey("departamento")) {
            dados.setDepartamento(textoOpcional(alteracoes.get("departamento")));
        }

        if (alteracoes.containsKey("salario")) {
            dados.setSalario(salarioDoPatch(alteracoes.get("salario")));
        }

        if (alteracoes.containsKey("cidade")) {
            dados.setCidade(textoOpcional(alteracoes.get("cidade")));
        }

        if (alteracoes.containsKey("status")) {
            dados.setStatus(statusDoPatch(alteracoes.get("status")));
        }

        validarDados(dados, true);

        funcionario.setNome(dados.getNome());
        funcionario.setEmail(dados.getEmail());
        funcionario.setTelefone(dados.getTelefone());
        funcionario.setCargo(dados.getCargo());
        funcionario.setDepartamento(dados.getDepartamento());
        funcionario.setSalario(dados.getSalario());
        funcionario.setCidade(dados.getCidade());
        funcionario.setStatus(dados.getStatus());

        return copiar(funcionario);
    }

    public synchronized void excluir(Integer id) {
        FuncionarioEntity funcionario = buscarEntidadePorId(id);
        funcionarios.remove(funcionario);
    }

    public synchronized List<FuncionarioEntity> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }

        String pesquisa = termo.trim().toLowerCase(Locale.ROOT);
        return funcionarios.stream()
                .filter(funcionario -> contem(funcionario.getNome(), pesquisa)
                        || contem(funcionario.getCargo(), pesquisa)
                        || contem(funcionario.getStatus() == null ? null : funcionario.getStatus().name(), pesquisa)
                        || contem(funcionario.getCidade(), pesquisa)
                        || contem(funcionario.getDepartamento(), pesquisa))
                .map(this::copiar)
                .toList();
    }

    private FuncionarioEntity buscarEntidadePorId(Integer id) {
        if (id == null) {
            throw new FuncionarioNaoEncontradoException(null);
        }

        return funcionarios.stream()
                .filter(funcionario -> id.equals(funcionario.getId()))
                .findFirst()
                .orElseThrow(() -> new FuncionarioNaoEncontradoException(id));
    }

    private void validarDados(FuncionarioEntity funcionario, boolean validarStatus) {
        funcionario.setNome(textoObrigatorio(funcionario.getNome(), "nome"));
        funcionario.setEmail(textoObrigatorio(funcionario.getEmail(), "e-mail").toLowerCase(Locale.ROOT));
        funcionario.setCargo(textoObrigatorio(funcionario.getCargo(), "cargo"));

        validarEmail(funcionario.getEmail());

        if (funcionario.getSalario() != null) {
            if (!Double.isFinite(funcionario.getSalario())) {
                throw new IllegalArgumentException("O salário deve ser um número válido.");
            }
            if (funcionario.getSalario() < 0) {
                throw new IllegalArgumentException("O salário não pode ser negativo.");
            }
        }

        if (validarStatus && funcionario.getStatus() == null) {
            throw new IllegalArgumentException("O status informado é inválido.");
        }
    }

    private void validarEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
    }

    private void validarEmailDuplicado(String email, Integer idIgnorado) {
        boolean duplicado = funcionarios.stream()
                .anyMatch(funcionario -> funcionario.getEmail() != null
                        && funcionario.getEmail().equalsIgnoreCase(email)
                        && (idIgnorado == null || !funcionario.getId().equals(idIgnorado)));

        if (duplicado) {
            throw new IllegalArgumentException("Já existe um candidato cadastrado com este e-mail.");
        }
    }

    private String textoObrigatorio(Object valor, String campo) {
        if (!(valor instanceof String texto) || texto.isBlank()) {
            throw new IllegalArgumentException("O " + campo + " é obrigatório.");
        }
        return texto.trim();
    }

    private String textoOpcional(Object valor) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof String texto)) {
            throw new IllegalArgumentException("Campo de texto possui valor inválido.");
        }
        String normalizado = texto.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private Double salarioDoPatch(Object valor) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof Number numero)) {
            throw new IllegalArgumentException("O salário deve ser um número válido.");
        }
        double salario = numero.doubleValue();
        if (!Double.isFinite(salario)) {
            throw new IllegalArgumentException("O salário deve ser um número válido.");
        }
        if (salario < 0) {
            throw new IllegalArgumentException("O salário não pode ser negativo.");
        }
        return salario;
    }

    private StatusFuncionario statusDoPatch(Object valor) {
        if (!(valor instanceof String texto) || texto.isBlank()) {
            throw new IllegalArgumentException("O status é obrigatório quando informado no PATCH.");
        }
        try {
            return StatusFuncionario.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Status inválido. Use EM_ANALISE, APROVADO, REPROVADO ou CONTRATADO.");
        }
    }

    private void normalizarTextos(FuncionarioEntity funcionario) {
        funcionario.setNome(trimOuOriginal(funcionario.getNome()));
        funcionario.setEmail(trimOuOriginal(funcionario.getEmail()));
        funcionario.setCargo(trimOuOriginal(funcionario.getCargo()));
        funcionario.setTelefone(trimOuNull(funcionario.getTelefone()));
        funcionario.setDepartamento(trimOuNull(funcionario.getDepartamento()));
        funcionario.setCidade(trimOuNull(funcionario.getCidade()));
    }

    private String trimOuOriginal(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String trimOuNull(String valor) {
        if (valor == null) {
            return null;
        }
        String resultado = valor.trim();
        return resultado.isEmpty() ? null : resultado;
    }

    private boolean contem(String valor, String pesquisa) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(pesquisa);
    }

    private FuncionarioEntity copiar(FuncionarioEntity funcionario) {
        return new FuncionarioEntity(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getEmail(),
                funcionario.getTelefone(),
                funcionario.getCargo(),
                funcionario.getDepartamento(),
                funcionario.getSalario(),
                funcionario.getCidade(),
                funcionario.getStatus());
    }

    private void carregarDadosDemonstracao() {
        cadastrar(demo("Marina Costa", "marina.costa@demo.com", "(11) 98821-4100", "Desenvolvedora Java",
                "Tecnologia", 7200.0, "São Paulo", StatusFuncionario.EM_ANALISE));
        cadastrar(demo("Lucas Ribeiro", "lucas.ribeiro@demo.com", "(21) 97731-2201", "Analista de Dados",
                "Dados", 6400.0, "Rio de Janeiro", StatusFuncionario.EM_ANALISE));
        cadastrar(demo("Beatriz Lima", "beatriz.lima@demo.com", null, "Product Designer",
                "Produto", 6800.0, "Belo Horizonte", StatusFuncionario.EM_ANALISE));
        cadastrar(demo("Rafael Souza", "rafael.souza@demo.com", "(41) 99915-3040", "QA Engineer",
                "Tecnologia", 5900.0, "Curitiba", StatusFuncionario.EM_ANALISE));

        cadastrar(demo("Camila Rocha", "camila.rocha@demo.com", "(31) 98800-4501", "Desenvolvedora Front-end",
                "Tecnologia", 7100.0, "Belo Horizonte", StatusFuncionario.APROVADO));
        cadastrar(demo("João Martins", "joao.martins@demo.com", "(11) 96630-1994", "Analista Financeiro",
                "Financeiro", 5300.0, "São Paulo", StatusFuncionario.APROVADO));
        cadastrar(demo("Ana Paula", "ana.paula@demo.com", null, "Business Partner RH",
                "Recursos Humanos", 6100.0, "Campinas", StatusFuncionario.APROVADO));

        cadastrar(demo("Diego Alves", "diego.alves@demo.com", "(19) 97744-3810", "Suporte Técnico",
                "Tecnologia", 3800.0, "Campinas", StatusFuncionario.REPROVADO));
        cadastrar(demo("Fernanda Melo", "fernanda.melo@demo.com", null, "Assistente Administrativo",
                "Operações", 3200.0, "Santos", StatusFuncionario.REPROVADO));

        cadastrar(demo("Gustavo Nunes", "gustavo.nunes@demo.com", "(11) 95520-4402", "Desenvolvedor Java Sênior",
                "Tecnologia", 10800.0, "São Paulo", StatusFuncionario.CONTRATADO));
        cadastrar(demo("Juliana Freitas", "juliana.freitas@demo.com", "(21) 98871-5560", "Gerente de Produto",
                "Produto", 11200.0, "Rio de Janeiro", StatusFuncionario.CONTRATADO));
        cadastrar(demo("Pedro Henrique", "pedro.henrique@demo.com", null, "Analista de Segurança",
                "Tecnologia", 8300.0, "São Paulo", StatusFuncionario.CONTRATADO));
    }

    private FuncionarioEntity demo(String nome, String email, String telefone, String cargo, String departamento,
            Double salario, String cidade, StatusFuncionario status) {
        return new FuncionarioEntity(null, nome, email, telefone, cargo, departamento, salario, cidade, status);
    }
}
