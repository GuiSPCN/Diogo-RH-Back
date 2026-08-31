package com.picpay.rh.service;

import com.picpay.rh.entity.FuncionarioEntity;
import com.picpay.rh.entity.StatusFuncionario;
import com.picpay.rh.exception.EmailDuplicadoException;
import com.picpay.rh.exception.FuncionarioNaoEncontradoException;
import com.picpay.rh.exception.RegraNegocioException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FuncionarioService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);

    private static final Map<StatusFuncionario, Set<StatusFuncionario>> TRANSICOES_PERMITIDAS = Map.of(
            StatusFuncionario.EM_ANALISE, EnumSet.of(StatusFuncionario.APROVADO, StatusFuncionario.REPROVADO),
            StatusFuncionario.APROVADO, EnumSet.of(StatusFuncionario.EM_ANALISE, StatusFuncionario.REPROVADO, StatusFuncionario.CONTRATADO),
            StatusFuncionario.REPROVADO, EnumSet.of(StatusFuncionario.EM_ANALISE),
            StatusFuncionario.CONTRATADO, EnumSet.noneOf(StatusFuncionario.class));

    private final ArrayList<FuncionarioEntity> funcionarios = new ArrayList<>();
    private Integer proximoId = 1;

    public FuncionarioService() {
        carregarDadosDemonstracao();
    }

    public FuncionarioEntity cadastrar(FuncionarioEntity funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("Os dados do funcionário são obrigatórios.");
        }

        // Todo novo candidato entra em análise. Aprovar, reprovar ou contratar
        // continua sendo uma ação humana posterior via PATCH.
        funcionario.setStatus(StatusFuncionario.EM_ANALISE);

        normalizar(funcionario);
        validarFuncionario(funcionario, true);
        validarEmailDuplicado(funcionario.getEmail(), null);

        funcionario.setId(proximoId++);
        funcionarios.add(funcionario);
        return funcionario;
    }

    public List<FuncionarioEntity> listarTodos() {
        return List.copyOf(funcionarios);
    }

    public FuncionarioEntity buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID deve ser um número inteiro positivo.");
        }

        return funcionarios.stream()
                .filter(funcionario -> funcionario.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new FuncionarioNaoEncontradoException(id));
    }

    public FuncionarioEntity atualizar(Integer id, FuncionarioEntity dadosAtualizados) {
        if (dadosAtualizados == null) {
            throw new IllegalArgumentException("Os dados completos do funcionário são obrigatórios.");
        }

        FuncionarioEntity funcionario = buscarPorId(id);
        normalizar(dadosAtualizados);
        validarFuncionario(dadosAtualizados, false);
        validarEmailDuplicado(dadosAtualizados.getEmail(), id);
        validarTransicao(funcionario, dadosAtualizados.getStatus(), dadosAtualizados);

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

    public FuncionarioEntity atualizarParcial(Integer id, Map<String, Object> alteracoes) {
        FuncionarioEntity funcionario = buscarPorId(id);

        if (alteracoes == null || alteracoes.isEmpty()) {
            throw new IllegalArgumentException("PATCH vazio: informe pelo menos um campo para atualizar.");
        }

        FuncionarioEntity candidato = copiar(funcionario);
        StatusFuncionario statusSolicitado = null;
        boolean statusPresente = alteracoes.containsKey("status");

        for (Map.Entry<String, Object> entrada : alteracoes.entrySet()) {
            String campo = entrada.getKey();
            Object valor = entrada.getValue();

            switch (campo) {
                case "nome" -> candidato.setNome(textoObrigatorio(valor, "O nome é obrigatório."));
                case "email" -> {
                    String email = normalizarEmail(textoObrigatorio(valor, "O e-mail é obrigatório."));
                    validarEmail(email);
                    validarEmailDuplicado(email, id);
                    candidato.setEmail(email);
                }
                case "telefone" -> candidato.setTelefone(textoOpcional(valor, "telefone"));
                case "cargo" -> candidato.setCargo(textoObrigatorio(valor, "O cargo é obrigatório."));
                case "departamento" -> candidato.setDepartamento(textoOpcional(valor, "departamento"));
                case "salario" -> candidato.setSalario(salarioPatch(valor));
                case "cidade" -> candidato.setCidade(textoOpcional(valor, "cidade"));
                case "status" -> statusSolicitado = statusPatch(valor);
                case "id" -> throw new IllegalArgumentException("O ID não pode ser alterado por PATCH.");
                default -> throw new IllegalArgumentException("Campo não reconhecido no PATCH: " + campo + ".");
            }
        }

        normalizar(candidato);
        validarFuncionario(candidato, false);
        if (statusPresente) {
            validarTransicao(funcionario, statusSolicitado, candidato);
            candidato.setStatus(statusSolicitado);
        }

        copiarDados(candidato, funcionario);
        return funcionario;
    }

    public void excluir(Integer id) {
        FuncionarioEntity funcionario = buscarPorId(id);
        funcionarios.remove(funcionario);
    }

    public List<FuncionarioEntity> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }

        String pesquisa = normalizarPesquisa(termo);

        return funcionarios.stream()
                .filter(funcionario -> normalizarPesquisa(funcionario.getNome()).contains(pesquisa)
                        || normalizarPesquisa(funcionario.getCargo()).contains(pesquisa)
                        || normalizarPesquisa(funcionario.getStatus().name()).contains(pesquisa)
                        || normalizarPesquisa(funcionario.getStatus().name().replace('_', ' ')).contains(pesquisa))
                .toList();
    }

    private FuncionarioEntity copiar(FuncionarioEntity origem) {
        return new FuncionarioEntity(
                origem.getId(), origem.getNome(), origem.getEmail(), origem.getTelefone(), origem.getCargo(),
                origem.getDepartamento(), origem.getSalario(), origem.getCidade(), origem.getStatus());
    }

    private void copiarDados(FuncionarioEntity origem, FuncionarioEntity destino) {
        destino.setNome(origem.getNome());
        destino.setEmail(origem.getEmail());
        destino.setTelefone(origem.getTelefone());
        destino.setCargo(origem.getCargo());
        destino.setDepartamento(origem.getDepartamento());
        destino.setSalario(origem.getSalario());
        destino.setCidade(origem.getCidade());
        destino.setStatus(origem.getStatus());
    }

    private void validarFuncionario(FuncionarioEntity funcionario, boolean permiteStatusDefault) {
        if (funcionario.getNome() == null || funcionario.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        if (funcionario.getEmail() == null || funcionario.getEmail().isBlank()) {
            throw new IllegalArgumentException("O e-mail é obrigatório.");
        }
        validarEmail(funcionario.getEmail());

        if (funcionario.getCargo() == null || funcionario.getCargo().isBlank()) {
            throw new IllegalArgumentException("O cargo é obrigatório.");
        }
        if (funcionario.getSalario() != null && funcionario.getSalario() < 0) {
            throw new IllegalArgumentException("O salário não pode ser negativo.");
        }
        if (!permiteStatusDefault && funcionario.getStatus() == null) {
            throw new IllegalArgumentException("O status é obrigatório em uma atualização completa (PUT).");
        }
    }

    private void validarEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
    }

    private void validarEmailDuplicado(String email, Integer idIgnorado) {
        boolean duplicado = funcionarios.stream()
                .anyMatch(funcionario -> funcionario.getEmail().equalsIgnoreCase(email)
                        && (idIgnorado == null || !funcionario.getId().equals(idIgnorado)));
        if (duplicado) {
            throw new EmailDuplicadoException(email);
        }
    }

    private void validarTransicao(FuncionarioEntity funcionario, StatusFuncionario novoStatus, FuncionarioEntity dadosParaContratacao) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("O status não pode ser nulo.");
        }
        StatusFuncionario atual = funcionario.getStatus();
        if (atual == novoStatus) {
            return;
        }
        if (!TRANSICOES_PERMITIDAS.getOrDefault(atual, Set.of()).contains(novoStatus)) {
            throw new RegraNegocioException(
                    "Transição de status não permitida: " + atual + " → " + novoStatus + ".");
        }
        if (novoStatus == StatusFuncionario.CONTRATADO && !prontoParaContratacao(dadosParaContratacao)) {
            throw new RegraNegocioException(
                    "Para contratar, informe departamento e um salário maior que zero.");
        }
    }

    private boolean prontoParaContratacao(FuncionarioEntity funcionario) {
        return funcionario.getDepartamento() != null
                && !funcionario.getDepartamento().isBlank()
                && funcionario.getSalario() != null
                && funcionario.getSalario() > 0;
    }

    private void normalizar(FuncionarioEntity funcionario) {
        funcionario.setNome(trim(funcionario.getNome()));
        funcionario.setEmail(normalizarEmail(funcionario.getEmail()));
        funcionario.setTelefone(trimOuNull(funcionario.getTelefone()));
        funcionario.setCargo(trim(funcionario.getCargo()));
        funcionario.setDepartamento(trimOuNull(funcionario.getDepartamento()));
        funcionario.setCidade(trimOuNull(funcionario.getCidade()));
    }

    private String normalizarEmail(String email) {
        String valor = trim(email);
        return valor == null ? null : valor.toLowerCase(Locale.ROOT);
    }

    private String trim(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String trimOuNull(String valor) {
        String normalizado = trim(valor);
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }

    private String textoObrigatorio(Object valor, String mensagem) {
        if (!(valor instanceof String texto) || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return texto.trim();
    }

    private String textoOpcional(Object valor, String campo) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof String texto)) {
            throw new IllegalArgumentException("O campo " + campo + " deve ser texto ou null.");
        }
        return trimOuNull(texto);
    }

    private Double salarioPatch(Object valor) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof Number numero)) {
            throw new IllegalArgumentException("O salário deve ser numérico ou null.");
        }
        double salario = numero.doubleValue();
        if (!Double.isFinite(salario) || salario < 0) {
            throw new IllegalArgumentException("O salário não pode ser negativo ou inválido.");
        }
        return salario;
    }

    private StatusFuncionario statusPatch(Object valor) {
        if (!(valor instanceof String texto) || texto.isBlank()) {
            throw new IllegalArgumentException("O status deve ser informado.");
        }
        try {
            return StatusFuncionario.valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Status inválido. Use EM_ANALISE, APROVADO, REPROVADO ou CONTRATADO.");
        }
    }

    private String normalizarPesquisa(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private void carregarDadosDemonstracao() {
        seed("Ana Souza", "ana.souza@rhflow.dev", "(11) 98888-1010", "Desenvolvedora Java", "Tecnologia", 7200.0, "São Paulo", StatusFuncionario.EM_ANALISE);
        seed("Bruno Lima", "bruno.lima@rhflow.dev", "(31) 97777-2020", "Analista de Dados", "Dados", 6800.0, "Belo Horizonte", StatusFuncionario.EM_ANALISE);
        seed("Carla Mendes", "carla.mendes@rhflow.dev", null, "Product Designer", "Produto", 6400.0, "São Paulo", StatusFuncionario.EM_ANALISE);
        seed("Diego Alves", "diego.alves@rhflow.dev", "(34) 96666-3030", "Analista Financeiro", "Financeiro", 5200.0, "Uberlândia", StatusFuncionario.EM_ANALISE);

        seed("Elisa Rocha", "elisa.rocha@rhflow.dev", "(21) 95555-4040", "Engenheira de Software", "Tecnologia", 9100.0, "Rio de Janeiro", StatusFuncionario.APROVADO);
        seed("Felipe Nunes", "felipe.nunes@rhflow.dev", "(11) 94444-5050", "People Analyst", "Pessoas", 5800.0, "São Paulo", StatusFuncionario.APROVADO);
        seed("Giulia Torres", "giulia.torres@rhflow.dev", null, "UX Researcher", null, null, "Curitiba", StatusFuncionario.APROVADO);

        seed("Henrique Costa", "henrique.costa@rhflow.dev", "(51) 93333-6060", "Analista de Riscos", "Riscos", 6100.0, "Porto Alegre", StatusFuncionario.REPROVADO);
        seed("Isabela Martins", "isabela.martins@rhflow.dev", null, "QA Engineer", "Tecnologia", 6000.0, "Recife", StatusFuncionario.REPROVADO);

        seed("João Ribeiro", "joao.ribeiro@rhflow.dev", "(85) 92222-7070", "Tech Lead", "Tecnologia", 12800.0, "Fortaleza", StatusFuncionario.CONTRATADO);
        seed("Karen Freitas", "karen.freitas@rhflow.dev", "(19) 91111-8080", "Coordenadora de RH", "Pessoas", 9800.0, "Campinas", StatusFuncionario.CONTRATADO);
        seed("Lucas Prado", "lucas.prado@rhflow.dev", "(62) 90000-9090", "Analista de Compliance", "Compliance", 7300.0, "Goiânia", StatusFuncionario.CONTRATADO);
    }

    private void seed(String nome, String email, String telefone, String cargo, String departamento,
            Double salario, String cidade, StatusFuncionario status) {
        FuncionarioEntity funcionario = new FuncionarioEntity(
                proximoId++, nome, email, telefone, cargo, departamento, salario, cidade, status);
        normalizar(funcionario);
        funcionarios.add(funcionario);
    }
}
