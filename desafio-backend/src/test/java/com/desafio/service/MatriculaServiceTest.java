package com.desafio.service;

import com.desafio.entity.Aluno;
import com.desafio.entity.Aula;
import com.desafio.entity.Curso;
import com.desafio.entity.Disciplina;
import com.desafio.entity.Horario;
import com.desafio.entity.Matricula;
import com.desafio.exception.CursoNaoAutorizadoException;
import com.desafio.exception.HorarioConflitanteException;
import com.desafio.exception.MatriculaDuplicadaException;
import com.desafio.exception.NotFoundException;
import com.desafio.exception.VagasEsgotadasException;
import com.desafio.repository.AlunoRepository;
import com.desafio.repository.AulaRepository;
import com.desafio.repository.MatriculaRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatriculaServiceTest {

    private MatriculaService service;

    private final AlunoRepository alunoRepository = mock(AlunoRepository.class);
    private final AulaRepository aulaRepository = mock(AulaRepository.class);
    private final MatriculaRepository matriculaRepository = mock(MatriculaRepository.class);

    private Aluno aluno;
    private Aula aula;

    @BeforeEach
    void setUp() {
        service = new MatriculaService();
        service.alunoRepository = alunoRepository;
        service.aulaRepository = aulaRepository;
        service.matriculaRepository = matriculaRepository;

        aluno = new Aluno();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");

        aula = new Aula();
        aula.setId(10L);
        aula.setVagas(5);
        aula.setDisciplina(disciplina(1L, "Matemática"));
        aula.setHorario(horario(1L, "Segunda", "08:00", "10:00"));
    }

    @Test
    void matricularComSucessoPersisteMatriculaSemReduzirVagas() {
        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);
        when(matriculaRepository.countByAula(10L)).thenReturn(0L);
        when(matriculaRepository.existsByAlunoAndAula(1L, 10L)).thenReturn(false);
        when(matriculaRepository.findAulasByAluno(1L)).thenReturn(List.of());

        Matricula matricula = service.matricular(1L, 10L);

        // vagas é a capacidade total; a ocupação vem da contagem de matrículas.
        assertEquals(5, aula.getVagas());
        assertEquals(aluno, matricula.getAluno());
        assertEquals(aula, matricula.getAula());
        verify(matriculaRepository).persist(matricula);
    }

    @Test
    void matricularComAlunoInexistenteLancaNotFoundException() {
        when(alunoRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.matricular(1L, 10L));
        verify(matriculaRepository, never()).persist(any(Matricula.class));
    }

    @Test
    void matricularComAulaInexistenteLancaNotFoundException() {
        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.matricular(1L, 10L));
        verify(matriculaRepository, never()).persist(any(Matricula.class));
    }

    @Test
    void matricularComVagasEsgotadasLancaVagasEsgotadasException() {
        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);
        when(matriculaRepository.countByAula(10L)).thenReturn(5L);

        assertThrows(VagasEsgotadasException.class, () -> service.matricular(1L, 10L));
        verify(matriculaRepository, never()).persist(any(Matricula.class));
    }

    @Test
    void matriculaDuplicadaLancaMatriculaDuplicadaException() {
        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);
        when(matriculaRepository.existsByAlunoAndAula(1L, 10L)).thenReturn(true);

        assertThrows(MatriculaDuplicadaException.class, () -> service.matricular(1L, 10L));
        verify(matriculaRepository, never()).persist(any(Matricula.class));
    }

    @Test
    void matricularComChoqueDeHorarioLancaHorarioConflitanteException() {
        Aula aulaConflitante = new Aula();
        aulaConflitante.setId(11L);
        aulaConflitante.setVagas(5);
        aulaConflitante.setDisciplina(disciplina(2L, "Português"));
        aulaConflitante.setHorario(horario(2L, "Segunda", "09:00", "11:00"));

        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);
        when(matriculaRepository.existsByAlunoAndAula(1L, 10L)).thenReturn(false);
        when(matriculaRepository.findAulasByAluno(1L)).thenReturn(List.of(aulaConflitante));

        assertThrows(HorarioConflitanteException.class, () -> service.matricular(1L, 10L));
        verify(matriculaRepository, never()).persist(any(Matricula.class));
    }

    @Test
    void matriculaSemChoqueQuandoHorarioEmDiaDiferente() {
        Aula aulaOutroDia = new Aula();
        aulaOutroDia.setId(11L);
        aulaOutroDia.setVagas(5);
        aulaOutroDia.setDisciplina(disciplina(2L, "Português"));
        aulaOutroDia.setHorario(horario(2L, "Terça", "09:00", "11:00"));

        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);
        when(matriculaRepository.countByAula(10L)).thenReturn(0L);
        when(matriculaRepository.existsByAlunoAndAula(1L, 10L)).thenReturn(false);
        when(matriculaRepository.findAulasByAluno(1L)).thenReturn(List.of(aulaOutroDia));

        Matricula matricula = service.matricular(1L, 10L);

        assertNotNull(matricula);
        assertEquals(5, aula.getVagas());
        verify(matriculaRepository).persist(matricula);
    }

    @Test
    void matriculaEmAulaAutorizadaParaOCursoPersiste() {
        Curso computacao = new Curso();
        computacao.setId(1L);
        computacao.setNome("Ciência da Computação");
        aluno.setCurso(computacao);
        aula.setCursosAutorizados(Set.of(computacao));

        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);
        when(matriculaRepository.countByAula(10L)).thenReturn(0L);
        when(matriculaRepository.existsByAlunoAndAula(1L, 10L)).thenReturn(false);
        when(matriculaRepository.findAulasByAluno(1L)).thenReturn(List.of());

        Matricula matricula = service.matricular(1L, 10L);

        assertNotNull(matricula);
        verify(matriculaRepository).persist(matricula);
    }

    @Test
    void matriculaEmAulaNaoAutorizadaParaOCursoLancaCursoNaoAutorizado() {
        Curso computacao = new Curso();
        computacao.setId(1L);
        Curso engenharia = new Curso();
        engenharia.setId(2L);
        aluno.setCurso(engenharia);
        aula.setCursosAutorizados(Set.of(computacao));

        when(alunoRepository.findById(1L)).thenReturn(aluno);
        when(aulaRepository.findById(eq(10L), any(LockModeType.class))).thenReturn(aula);

        assertThrows(CursoNaoAutorizadoException.class, () -> service.matricular(1L, 10L));
        verify(matriculaRepository, never()).persist(any(Matricula.class));
    }

    private static Disciplina disciplina(Long id, String nome) {
        Disciplina disciplina = new Disciplina();
        disciplina.setId(id);
        disciplina.setNome(nome);
        return disciplina;
    }

    private static Horario horario(Long id, String dia, String inicio, String fim) {
        Horario horario = new Horario();
        horario.setId(id);
        horario.setDiaSemana(dia);
        horario.setHoraInicio(inicio);
        horario.setHoraFim(fim);
        return horario;
    }
}