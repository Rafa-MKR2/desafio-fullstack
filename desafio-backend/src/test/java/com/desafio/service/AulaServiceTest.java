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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AulaServiceTest {

    private static final long COORD_ID = 1L;

    private AulaService service;

    private final AulaRepository aulaRepository = mock(AulaRepository.class);
    private final DisciplinaRepository disciplinaRepository = mock(DisciplinaRepository.class);
    private final ProfessorRepository professorRepository = mock(ProfessorRepository.class);
    private final HorarioRepository horarioRepository = mock(HorarioRepository.class);
    private final MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);
    private final CursoRepository cursoRepository = mock(CursoRepository.class);
    private final CoordenadorRepository coordenadorRepository = mock(CoordenadorRepository.class);

    private Disciplina disciplina;
    private Professor professor;
    private Horario horario;
    private Coordenador coordenador;

    @BeforeEach
    void setUp() {
        service = new AulaService();
        service.aulaRepository = aulaRepository;
        service.disciplinaRepository = disciplinaRepository;
        service.professorRepository = professorRepository;
        service.horarioRepository = horarioRepository;
        service.matriculaRepository = matriculaRepository;
        service.cursoRepository = cursoRepository;
        service.coordenadorRepository = coordenadorRepository;

        coordenador = new Coordenador();
        coordenador.setId(COORD_ID);
        coordenador.setEmail("coordenador1@email.com");

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

        when(coordenadorRepository.findById(COORD_ID)).thenReturn(coordenador);
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

        Aula aula = service.criar(request(), COORD_ID);

        assertEquals("Matemática", aula.getDisciplina().getNome());
        assertEquals("Ana Paula", aula.getProfessor().getNome());
        assertEquals("Segunda", aula.getHorario().getDiaSemana());
        assertEquals(30, aula.getVagas());
        verify(aulaRepository).persist(aula);
    }

    @Test
    void criarComCursosAutorizadosDefineCursosNaAula() {
        Curso computacao = new Curso();
        computacao.setId(1L);
        computacao.setNome("Ciência da Computação");

        AulaRequest req = request();
        req.setCursoIds(List.of(1L));

        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);
        when(cursoRepository.findById(1L)).thenReturn(computacao);
        when(aulaRepository.listarPorProfessorMesmoHorario(1L, "Segunda", "08:00", "10:00"))
                .thenReturn(List.of());

        Aula aula = service.criar(req, COORD_ID);

        assertEquals(1, aula.getCursosAutorizados().size());
        assertTrue(aula.getCursosAutorizados().contains(computacao));
    }

    @Test
    void criarComCursoInexistenteLancaIllegalArgumentException() {
        AulaRequest req = request();
        req.setCursoIds(List.of(999L));

        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);
        when(cursoRepository.findById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.criar(req, COORD_ID));
        verify(aulaRepository, never()).persist(any(Aula.class));
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

        assertThrows(ProfessorConflitanteException.class, () -> service.criar(request(), COORD_ID));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void criarComDisciplinaInexistenteLancaIllegalArgumentException() {
        when(disciplinaRepository.findById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.criar(request(), COORD_ID));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void criarComProfessorQueNaoLecionaDisciplinaLancaIllegalArgumentException() {
        professor.setDisciplinas(Set.of());

        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);

        assertThrows(IllegalArgumentException.class, () -> service.criar(request(), COORD_ID));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void criarComHorarioInexistenteLancaIllegalArgumentException() {
        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> service.criar(request(), COORD_ID));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void atualizarComVagasMenorQueMatriculadosLancaIllegalArgumentException() {
        Aula aulaExistente = new Aula();
        aulaExistente.setId(10L);
        aulaExistente.setVagas(30);
        aulaExistente.setCoordenador(coordenador);

        AulaRequest request = request();
        request.setVagas(2);

        when(aulaRepository.findById(10L)).thenReturn(aulaExistente);
        when(disciplinaRepository.findById(1L)).thenReturn(disciplina);
        when(professorRepository.findById(1L)).thenReturn(professor);
        when(horarioRepository.findById(1L)).thenReturn(horario);
        when(aulaRepository.listarPorProfessorMesmoHorario(1L, "Segunda", "08:00", "10:00"))
                .thenReturn(List.of());
        when(matriculaRepository.countByAula(10L)).thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> service.atualizar(10L, request, COORD_ID));
        verify(aulaRepository, never()).persist(any(Aula.class));
    }

    @Test
    void atualizarAulaInexistenteLancaNotFoundException() {
        when(aulaRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.atualizar(10L, request(), COORD_ID));
    }

    @Test
    void atualizarAulaDeOutroCoordenadorLancaNotFoundException() {
        Aula aulaDeOutro = new Aula();
        aulaDeOutro.setId(10L);
        Coordenador outro = new Coordenador();
        outro.setId(99L);
        aulaDeOutro.setCoordenador(outro);

        when(aulaRepository.findById(10L)).thenReturn(aulaDeOutro);

        assertThrows(NotFoundException.class, () -> service.atualizar(10L, request(), COORD_ID));
    }

    @Test
    void excluirComMatriculasLancaIllegalStateException() {
        Aula aulaExistente = new Aula();
        aulaExistente.setId(10L);
        aulaExistente.setCoordenador(coordenador);

        when(aulaRepository.findById(10L)).thenReturn(aulaExistente);
        when(matriculaRepository.countByAula(10L)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.excluir(10L, COORD_ID));
        verify(aulaRepository, never()).delete(any(Aula.class));
    }

    @Test
    void excluirSemMatriculasMarcaComoInativo() {
        Aula aulaExistente = new Aula();
        aulaExistente.setId(10L);
        aulaExistente.setAtivo(true);
        aulaExistente.setCoordenador(coordenador);

        when(aulaRepository.findById(10L)).thenReturn(aulaExistente);
        when(matriculaRepository.countByAula(10L)).thenReturn(0L);

        service.excluir(10L, COORD_ID);

        assertFalse(aulaExistente.isAtivo());
        verify(aulaRepository).persist(aulaExistente);
        verify(aulaRepository, never()).delete(any(Aula.class));
    }

    @Test
    void buscarPorIdInexistenteLancaNotFoundException() {
        when(aulaRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.buscarPorId(10L, COORD_ID));
    }
}