package com.desafio.service;

import com.desafio.dto.AulaRequest;
import com.desafio.entity.Aula;
import com.desafio.entity.Coordenador;
import com.desafio.entity.Curso;
import com.desafio.entity.Disciplina;
import com.desafio.entity.Horario;
import com.desafio.entity.Professor;
import com.desafio.exception.NotFoundException;
import com.desafio.exception.ProfessorConflitanteException;
import com.desafio.repository.AulaRepository;
import com.desafio.repository.CoordenadorRepository;
import com.desafio.repository.CursoRepository;
import com.desafio.repository.DisciplinaRepository;
import com.desafio.repository.HorarioRepository;
import com.desafio.repository.MatriculaRepository;
import com.desafio.repository.ProfessorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    CursoRepository cursoRepository;

    @Inject
    CoordenadorRepository coordenadorRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    @Transactional
    public Aula criar(AulaRequest request, Long coordenadorId) {
        Coordenador coordenador = coordenadorRepository.findById(coordenadorId);
        if (coordenador == null) {
            throw new NotFoundException("Coordenador não encontrado: " + coordenadorId);
        }
        validarReferencias(request);
        validarConflitoProfessor(null, request);

        Aula aula = new Aula();
        aula.setCoordenador(coordenador);
        aula.setDisciplina(disciplinaRepository.findById(request.getDisciplinaId()));
        aula.setProfessor(professorRepository.findById(request.getProfessorId()));
        aula.setHorario(horarioRepository.findById(request.getHorarioId()));
        aula.setVagas(request.getVagas());
        aula.setCursosAutorizados(validarCursos(request));

        aulaRepository.persist(aula);
        return aula;
    }

    @Transactional
    public Aula atualizar(Long aulaId, AulaRequest request, Long coordenadorId) {
        Aula aula = aulaRepository.findById(aulaId);
        if (aula == null) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }
        validarDono(aula, coordenadorId);

        validarReferencias(request);
        validarConflitoProfessor(aulaId, request);

        aula.setDisciplina(disciplinaRepository.findById(request.getDisciplinaId()));
        aula.setProfessor(professorRepository.findById(request.getProfessorId()));
        aula.setHorario(horarioRepository.findById(request.getHorarioId()));
        aula.setCursosAutorizados(validarCursos(request));

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
    public void excluir(Long aulaId, Long coordenadorId) {
        Aula aula = aulaRepository.findById(aulaId);
        if (aula == null) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }
        validarDono(aula, coordenadorId);

        if (matriculaRepository.countByAula(aulaId) > 0) {
            throw new IllegalStateException("Não é possível excluir uma aula com matrículas vinculadas");
        }

        // Exclusão lógica: a aula deixa de aparecer e de aceitar matrículas.
        aula.setAtivo(false);
        aulaRepository.persist(aula);
    }

    public Aula buscarPorId(Long aulaId, Long coordenadorId) {
        Aula aula = aulaRepository.findById(aulaId);
        if (aula == null || !aula.isAtivo()) {
            throw new NotFoundException("Aula não encontrada: " + aulaId);
        }
        if (coordenadorId != null) {
            validarDono(aula, coordenadorId);
        }
        return aula;
    }

    public List<Aula> listarTodos() {
        return aulaRepository.listarAtivas();
    }

    public List<Aula> listarComFiltros(Long disciplinaId, Long professorId, String diaSemana,
                                       Long cursoId, Long horarioId, Boolean vagasDisponiveis,
                                       Long coordenadorId) {
        return aulaRepository.listarComFiltros(
                disciplinaId, professorId, diaSemana, cursoId, horarioId, vagasDisponiveis,
                coordenadorId);
    }

    public List<Aula> listarPorDisciplina(Long disciplinaId) {
        return aulaRepository.listarPorDisciplina(disciplinaId);
    }

    public List<Aula> listarPorProfessor(Long professorId) {
        return aulaRepository.listarPorProfessor(professorId);
    }

    public long contarMatriculados(Long aulaId) {
        return matriculaRepository.countByAula(aulaId);
    }

    public Map<Long, Long> contarMatriculadosPorAula(Collection<Long> aulaIds) {
        return matriculaRepository.countByAulaIds(aulaIds);
    }

    /**
     * Garante que a aula pertence ao coordenador autenticado. Usa 404 (em vez
     * de 403) para não revelar a existência de aulas de outros coordenadores.
     */
    private void validarDono(Aula aula, Long coordenadorId) {
        if (!aula.getCoordenador().getId().equals(coordenadorId)) {
            throw new NotFoundException("Aula não encontrada: " + aula.getId());
        }
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

        boolean leciona = professor.getDisciplinas().stream()
                .anyMatch(d -> d.getId().equals(disciplina.getId()));
        if (!leciona) {
            throw new IllegalArgumentException(
                    "Professor não leciona a disciplina informada: " + disciplina.getNome());
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

    /**
     * Resolve os cursos autorizados da aula a partir do request. Ausência de
     * cursoIds significa que a aula é aberta a todos os cursos.
     */
    private Set<Curso> validarCursos(AulaRequest request) {
        if (request.getCursoIds() == null || request.getCursoIds().isEmpty()) {
            return new HashSet<>();
        }
        Set<Curso> cursos = new LinkedHashSet<>();
        for (Long cursoId : request.getCursoIds()) {
            Curso curso = cursoRepository.findById(cursoId);
            if (curso == null) {
                throw new IllegalArgumentException("Curso não encontrado: " + cursoId);
            }
            cursos.add(curso);
        }
        return cursos;
    }
}