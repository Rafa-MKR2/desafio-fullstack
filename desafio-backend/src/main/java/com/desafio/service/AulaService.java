package com.desafio.service;

import com.desafio.dto.AulaRequest;
import com.desafio.entity.Aula;
import com.desafio.entity.Disciplina;
import com.desafio.entity.Horario;
import com.desafio.entity.Professor;
import com.desafio.exception.NotFoundException;
import com.desafio.exception.ProfessorConflitanteException;
import com.desafio.repository.AulaRepository;
import com.desafio.repository.DisciplinaRepository;
import com.desafio.repository.HorarioRepository;
import com.desafio.repository.MatriculaRepository;
import com.desafio.repository.ProfessorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class AulaService {

    @Inject
    AulaRepository aulaRepository;

    @Inject
    DisciplinaRepository disciplinaRepository;

    @Inject
    ProfessorRepository professorRepository;

    @Inject
    HorarioRepository horarioRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    @Transactional
    public Aula criar(AulaRequest request) {
        validarReferencias(request);
        validarConflitoProfessor(null, request);

        Aula aula = new Aula();
        aula.setDisciplina(disciplinaRepository.findById(request.getDisciplinaId()));
        aula.setProfessor(professorRepository.findById(request.getProfessorId()));
        aula.setHorario(horarioRepository.findById(request.getHorarioId()));
        aula.setVagas(request.getVagas());

        aulaRepository.persist(aula);
        return aula;
    }

    @Transactional
    public Aula atualizar(Long aulaId, AulaRequest request) {
        Aula aula = aulaRepository.findById(aulaId);
        if (aula == null) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }

        validarReferencias(request);
        validarConflitoProfessor(aulaId, request);

        aula.setDisciplina(disciplinaRepository.findById(request.getDisciplinaId()));
        aula.setProfessor(professorRepository.findById(request.getProfessorId()));
        aula.setHorario(horarioRepository.findById(request.getHorarioId()));

        Integer totalMatriculados = Math.toIntExact(matriculaRepository.countByAula(aulaId));
        if (request.getVagas() < totalMatriculados) {
            throw new IllegalArgumentException(
                    "Número de vagas não pode ser menor que o de matriculados: " + totalMatriculados);
        }
        aula.setVagas(request.getVagas());

        aulaRepository.persist(aula);
        return aula;
    }

    @Transactional
    public void excluir(Long aulaId) {
        Aula aula = aulaRepository.findById(aulaId);
        if (aula == null) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }

        if (matriculaRepository.countByAula(aulaId) > 0) {
            throw new IllegalStateException("Não é possível excluir uma aula com matrículas vinculadas");
        }

        aulaRepository.delete(aula);
    }

    public Aula buscarPorId(Long aulaId) {
        Aula aula = aulaRepository.findById(aulaId);
        if (aula == null) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }
        return aula;
    }

    public List<Aula> listarTodos() {
        return aulaRepository.listAll();
    }

    public List<Aula> listarPorDisciplina(Long disciplinaId) {
        return aulaRepository.listarPorDisciplina(disciplinaId);
    }

    public List<Aula> listarPorProfessor(Long professorId) {
        return aulaRepository.listarPorProfessor(professorId);
    }

    private void validarReferencias(AulaRequest request) {
        Disciplina disciplina = disciplinaRepository.findById(request.getDisciplinaId());
        if (disciplina == null) {
            throw new IllegalArgumentException("Disciplina não encontrada: " + request.getDisciplinaId());
        }

        Professor professor = professorRepository.findById(request.getProfessorId());
        if (professor == null) {
            throw new IllegalArgumentException("Professor não encontrado: " + request.getProfessorId());
        }

        Horario horario = horarioRepository.findById(request.getHorarioId());
        if (horario == null) {
            throw new IllegalArgumentException("Horário não encontrado: " + request.getHorarioId());
        }
    }

    private void validarConflitoProfessor(Long aulaId, AulaRequest request) {
        Horario horario = horarioRepository.findById(request.getHorarioId());
        List<Aula> conflitantes = aulaRepository.listarPorProfessorMesmoHorario(
                request.getProfessorId(), horario.getDiaSemana(),
                horario.getHoraInicio(), horario.getHoraFim());

        for (Aula conflitante : conflitantes) {
            if (aulaId != null && conflitante.getId().equals(aulaId)) {
                continue;
            }
            throw new ProfessorConflitanteException(
                    "Professor já possui aula neste horário (aula: " + conflitante.getId() + ")");
        }
    }
}