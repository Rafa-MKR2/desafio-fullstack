package com.desafio.integration;

import com.desafio.entity.Aluno;
import com.desafio.entity.Coordenador;
import com.desafio.entity.Curso;
import com.desafio.entity.Disciplina;
import com.desafio.entity.Horario;
import com.desafio.entity.Professor;
import com.desafio.repository.AlunoRepository;
import com.desafio.repository.CoordenadorRepository;
import com.desafio.repository.CursoRepository;
import com.desafio.repository.DisciplinaRepository;
import com.desafio.repository.HorarioRepository;
import com.desafio.repository.ProfessorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Bean exclusivo de teste: limpa as tabelas e recria uma base mínima e
 * determinística (mesmas regras do init.sql) antes de cada teste.
 */
@ApplicationScoped
public class TestDataSeeder {

    @Inject
    EntityManager em;

    @Inject
    CursoRepository cursoRepository;

    @Inject
    DisciplinaRepository disciplinaRepository;

    @Inject
    ProfessorRepository professorRepository;

    @Inject
    HorarioRepository horarioRepository;

    @Inject
    CoordenadorRepository coordenadorRepository;

    @Inject
    AlunoRepository alunoRepository;

    @Transactional
    public void resetarBase() {
        em.createNativeQuery(
                "TRUNCATE TABLE matricula, aula_curso, aula, professor_disciplina, aluno, " +
                "coordenador, professor, horario, disciplina, curso RESTART IDENTITY CASCADE")
                .executeUpdate();

        Curso computacao = novoCurso("Ciência da Computação");
        Curso engenharia = novoCurso("Engenharia Civil");

        Disciplina matematica = novaDisciplina("Matemática");
        Disciplina portugues = novaDisciplina("Português");
        novaDisciplina("História");

        Professor ana = novoProfessor("Ana Paula", matematica);
        novoProfessor("Carlos Alberto", portugues);

        novoHorario("Segunda", "08:00", "10:00");
        novoHorario("Segunda", "09:00", "11:00");
        novoHorario("Terça", "08:00", "10:00");
        novoHorario("Quarta", "10:00", "12:00");

        novoCoordenador("Dr. José", "coordenador1@email.com", computacao);
        novoAluno("João Silva", "aluno1@email.com", computacao);
        novoAluno("Maria Santos", "aluno2@email.com", computacao);
        novoAluno("Pedro Costa", "aluno3@email.com", engenharia);
    }

    public Long cursoId(String nome) {
        return cursoRepository.find("nome", nome).firstResult().getId();
    }

    public Long disciplinaId(String nome) {
        return disciplinaRepository.find("nome", nome).firstResult().getId();
    }

    public Long professorId(String nome) {
        return professorRepository.find("nome", nome).firstResult().getId();
    }

    public Long horarioId(String diaSemana, String inicio, String fim) {
        return horarioRepository.find("diaSemana = ?1 and horaInicio = ?2 and horaFim = ?3",
                diaSemana, inicio, fim).firstResult().getId();
    }

    public Long alunoId(String email) {
        return alunoRepository.findByEmail(email).orElseThrow().getId();
    }

    private Curso novoCurso(String nome) {
        Curso curso = new Curso();
        curso.setNome(nome);
        em.persist(curso);
        return curso;
    }

    private Disciplina novaDisciplina(String nome) {
        Disciplina disciplina = new Disciplina();
        disciplina.setNome(nome);
        disciplina.setCargaHoraria(60);
        em.persist(disciplina);
        return disciplina;
    }

    private Professor novoProfessor(String nome, Disciplina disciplina) {
        Professor professor = new Professor();
        professor.setNome(nome);
        professor.getDisciplinas().add(disciplina);
        em.persist(professor);
        return professor;
    }

    private void novoHorario(String diaSemana, String inicio, String fim) {
        Horario horario = new Horario();
        horario.setDiaSemana(diaSemana);
        horario.setHoraInicio(inicio);
        horario.setHoraFim(fim);
        em.persist(horario);
    }

    private void novoCoordenador(String nome, String email, Curso curso) {
        Coordenador coordenador = new Coordenador();
        coordenador.setNome(nome);
        coordenador.setEmail(email);
        coordenador.setCurso(curso);
        em.persist(coordenador);
    }

    private void novoAluno(String nome, String email, Curso curso) {
        Aluno aluno = new Aluno();
        aluno.setNome(nome);
        aluno.setEmail(email);
        aluno.setCurso(curso);
        em.persist(aluno);
    }
}
