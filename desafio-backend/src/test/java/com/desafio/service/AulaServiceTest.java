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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AulaServiceTest {

    private AulaService service;

    private final AulaRepository aulaRepository = mock(AulaRepository.class);
    private final DisciplinaRepository disciplinaRepository = mock(DisciplinaRepository.class);
    private final ProfessorRepository professorRepository = mock(ProfessorRepository.class);
    private final HorarioRepository horarioRepository = mock(HorarioRepository.class);
    private final MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);

    private Disciplina disciplina;
    private Professor professor;
    private Horario horario;

    @BeforeEach
    void setUp() {
        service = new AulaService();
        service.aulaRepository = aulaRepository;
        service.disciplinaRepository = disciplinaRepository;
        service.professorRepository = professorRepository;
        service.horarioRepository = horarioRepository;
        service.matriculaRepository = matriculaRepository;

        disciplina = new Disciplina();
        disciplina.setId(1L);
        disciplina.setNome("Matemática");

        professor = new Professor();
        professor.setId(1L);
        professor.setNome("Ana Paula");
        professor.setDisciplinas(Set.of(disciplina));

        horario = new Horario();
        horario.setId(1L);
        horario.setDiaSemana("Segunda");
        horario.setHoraInicio("08:00");
        horario.setHoraFim("10:00");
    }

    private AulaRequest request() {
        AulaRequest request = new AulaRequest();
        request.setDisciplinaId(1L);
        request.setProfessorId(1L);
        request.setHorarioId(1L);
        request.setVagas(30);
        return request;
    }

    @Test
    void criarComSucesso() {
        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);
        when(aulaRepository.listarPorProfessorMesmoHorario(1L, "Segunda", "08:00", "10:00"))
                .thenReturn(List.of());

        Aula aula = service.criar(request());

        assertEquals("Matemática", aula.getDisciplina().getNome());
        assertEquals("Ana Paula", aula.getProfessor().getNome());
        assertEquals("Segunda", aula.getHorario().getDiaSemana());
        assertEquals(30, aula.getVagas());
        verify(aulaRepository).persist(aula);
    }

    @Test
    void criarComProfessorConflitanteLancaProfessorConflitanteException() {
        Aula outraAula = new Aula();
        outraAula.setId(99L);

        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);
        when(aulaRepository.listarPorProfessorMesmoHorario(1L, "Segunda", "08:00", "10:00"))
                .thenReturn(List.of(outraAula));

        assertThrows(ProfessorConflitanteException.class, () -> service.criar(request()));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void criarComDisciplinaInexistenteLancaIllegalArgumentException() {
        when(disciplinaRepository.findById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.criar(request()));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void criarComProfessorQueNaoLecionaDisciplinaLancaIllegalArgumentException() {
        professor.setDisciplinas(Set.of());

        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);

        assertThrows(IllegalArgumentException.class, () -> service.criar(request()));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void criarComHorarioInexistenteLancaIllegalArgumentException() {
        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.criar(request()));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void atualizarComVagasMenorQueMatriculadosLancaIllegalArgumentException() {
        Aula aulaExistente = new Aula();
        aulaExistente.setId(10L);
        aulaExistente.setVagas(30);

        AulaRequest request = request();
        request.setVagas(2);

        when(aulaRepository.findById(10L)).thenReturn(aulaExistente);
        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);
        when(aulaRepository.listarPorProfessorMesmoHorario(1L, "Segunda", "08:00", "10:00"))
                .thenReturn(List.of());
        when(matriculaRepository.countByAula(10L)).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, request));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void atualizarAulaInexistenteLancaNotFoundException() {
        when(aulaRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.atualizar(10L, request()));
    }

    @Test
    void excluirComMatriculasLancaIllegalStateException() {
        Aula aulaExistente = new Aula();
        aulaExistente.setId(10L);

        when(aulaRepository.findById(10L)).thenReturn(aulaExistente);
        when(matriculaRepository.countByAula(10L)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.excluir(10L));
        verify(aulaRepository, never()).delete(any(Aula.class));
    }

    @Test
    void excluirSemMatriculasDeleta() {
        Aula aulaExistente = new Aula();
        aulaExistente.setId(10L);

        when(aulaRepository.findById(10L)).thenReturn(aulaExistente);
        when(matriculaRepository.countByAula(10L)).thenReturn(0L);

        service.excluir(10L);

        verify(aulaRepository).delete(aulaExistente);
    }

    @Test
    void buscarPorIdInexistenteLancaNotFoundException() {
        when(aulaRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.buscarPorId(10L));
    }
}