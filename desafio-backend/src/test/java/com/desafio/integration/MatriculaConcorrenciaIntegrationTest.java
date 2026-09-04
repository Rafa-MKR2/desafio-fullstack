package com.desafio.integration;

import com.desafio.dto.AulaRequest;
import com.desafio.entity.Aula;
import com.desafio.exception.VagasEsgotadasException;
import com.desafio.repository.AulaRepository;
import com.desafio.repository.MatriculaRepository;
import com.desafio.service.AulaService;
import com.desafio.service.MatriculaService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prova o requisito crítico de concorrência: matrículas simultâneas nunca
 * podem ultrapassar as vagas da aula (lock pessimista + versão otimista).
 */
@QuarkusTest
class MatriculaConcorrenciaIntegrationTest {

    @Inject
    TestDataSeeder seeder;

    @Inject
    AulaService aulaService;

    @Inject
    MatriculaService matriculaService;

    @Inject
    AulaRepository aulaRepository;

    @Inject
    MatriculaRepository matriculaRepository;

    @BeforeEach
    void setUp() {
        seeder.resetarBase();
    }

    @Test
    void matriculasConcorrentesNaoUltrapassamVagaUnica() throws Exception {
        Long aulaId = criarAulaComVagas(1);
        List<Long> alunos = List.of(
                seeder.alunoId("aluno1@email.com"),
                seeder.alunoId("aluno2@email.com"));

        List<String> resultados = matricularEmParalelo(alunos, aulaId);

        assertEquals(1, resultados.stream().filter("OK"::equals).count());
        assertEquals(1, resultados.stream().filter("ESGOTADA"::equals).count());
        assertEquals(1, aulaRepository.findById(aulaId).getVagas());
        assertEquals(1, matriculaRepository.countByAula(aulaId));
    }

    @Test
    void matriculasConcorrentesNaoUltrapassamDuasVagas() throws Exception {
        Long aulaId = criarAulaComVagas(2);
        List<Long> alunos = List.of(
                seeder.alunoId("aluno1@email.com"),
                seeder.alunoId("aluno2@email.com"),
                seeder.alunoId("aluno3@email.com"));

        List<String> resultados = matricularEmParalelo(alunos, aulaId);

        assertEquals(2, resultados.stream().filter("OK"::equals).count());
        assertEquals(1, resultados.stream().filter("ESGOTADA"::equals).count());
        assertEquals(2, aulaRepository.findById(aulaId).getVagas());
        assertEquals(2, matriculaRepository.countByAula(aulaId));
    }

    private Long criarAulaComVagas(int vagas) {
        AulaRequest request = new AulaRequest();
        request.setDisciplinaId(seeder.disciplinaId("Matemática"));
        request.setProfessorId(seeder.professorId("Ana Paula"));
        request.setHorarioId(seeder.horarioId("Segunda", "08:00", "10:00"));
        request.setVagas(vagas);
        Aula aula = aulaService.criar(request);
        assertTrue(aula.getId() != null);
        return aula.getId();
    }

    private List<String> matricularEmParalelo(List<Long> alunoIds, Long aulaId) throws Exception {
        int total = alunoIds.size();
        ExecutorService pool = Executors.newFixedThreadPool(total);
        CyclicBarrier largada = new CyclicBarrier(total);
        try {
            List<Future<String>> futures = new ArrayList<>();
            for (Long alunoId : alunoIds) {
                futures.add(pool.submit(() -> {
                    largada.await(10, TimeUnit.SECONDS);
                    try {
                        matriculaService.matricular(alunoId, aulaId);
                        return "OK";
                    } catch (VagasEsgotadasException e) {
                        return "ESGOTADA";
                    }
                }));
            }
            List<String> resultados = new ArrayList<>();
            for (Future<String> future : futures) {
                resultados.add(future.get(30, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            pool.shutdownNow();
        }
    }
}
