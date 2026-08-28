package com.picpay.rh.entity;

public class FuncionarioEntity {
    private Integer id;
    private String nome;
    private String email;
    private String telefone;
    private String cargo;
    private String departamento;
    private Double salario;
    private String cidade;
    private String status;
    // O atributo status poderá assumir valores como: EM_ANALISE, APROVADO,
    // REPROVADO e
    // CONTRATADO. Você poderá criar outros atributos caso considere necessário.

    public FuncionarioEntity() {
    }

    public FuncionarioEntity(Integer id, String nome, String email, String telefone, String cargo, String departamento,
            Double salario, String cidade, String status) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cargo = cargo;
        this.departamento = departamento;
        this.salario = salario;
        this.cidade = cidade;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public Double getSalario() {
        return salario;
    }

    public void setSalario(Double salario) {
        this.salario = salario;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
// id
// nome
// email
// telefone
// cargo
// departamento
// salario
// cidade
// status
