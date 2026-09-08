package com.desafio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "aula")
public class Aula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "disciplina_id")
    private Disciplina disciplina;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "horario_id")
    private Horario horario;

    @Column(nullable = false)
    private Integer vagas;

    @ManyToMany
    @JoinTable(
            name = "aula_curso",
            joinColumns = @JoinColumn(name = "aula_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id"))
    private Set<Curso> cursosAutorizados = new HashSet<>();

    @Column(nullable = false)
    private boolean ativo = true;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Curso> getCursosAutorizados() {
        return cursosAutorizados;
    }

    public void setCursosAutorizados(Set<Curso> cursosAutorizados) {
        this.cursosAutorizados = cursosAutorizados;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public Integer getVagas() {
        return vagas;
    }

    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }
}