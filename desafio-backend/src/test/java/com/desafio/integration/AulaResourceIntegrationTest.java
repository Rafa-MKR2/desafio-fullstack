package com.desafio.integration;

import com.desafio.dto.AulaRequest;
import com.desafio.service.AulaService;
import com.desafio.service.MatriculaService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AulaResourceIntegrationTest {

    @Inject
    TestDataSeeder seeder;

    @Inject
    MatriculaService matriculaService;

    @Inject
    AulaService aulaService;

    @BeforeEach
    void setUp() {
        seeder.resetarBase();
    }

    private Long criarAulaComoCoordenador(Long disciplinaId, Long professorId,
                                          Long horarioId, int vagas) {
        String body = """
                {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": %d}
                """.formatted(disciplinaId, professorId, horarioId, vagas);
        return ((Number) given()
                .auth().none()
                .contentType("application/json")
                .body(body)
                .when().post("/aulas")
                .then().statusCode(201)
                .extract().path("id")).longValue();
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void criarAulaComSucessoRetorna201ComOcupacaoZerada() {
        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 30}
                        """.formatted(
                        seeder.disciplinaId("Matemática"),
                        seeder.professorId("Ana Paula"),
                        seeder.horarioId("Segunda", "08:00", "10:00")))
                .when().post("/aulas")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("disciplinaNome", equalTo("Matemática"))
                .body("vagas", equalTo(30))
                .body("vagasOcupadas", equalTo(0))
                .body("vagasRestantes", equalTo(30));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void criarAulaComCursosAutorizadosRetornaCursosNaResposta() {
        Long computacao = seeder.cursoId("Ciência da Computação");
        Long engenharia = seeder.cursoId("Engenharia Civil");

        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 30, "cursoIds": [%d, %d]}
                        """.formatted(
                        seeder.disciplinaId("Matemática"),
                        seeder.professorId("Ana Paula"),
                        seeder.horarioId("Segunda", "08:00", "10:00"),
                        computacao, engenharia))
                .when().post("/aulas")
                .then()
                .statusCode(201)
                .body("cursoIds", hasItems(computacao.intValue(), engenharia.intValue()));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void alunoNaoPodeCriarAulaRetorna403() {
        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": 1, "professorId": 1, "horarioId": 1, "vagas": 30}
                        """)
                .when().post("/aulas")
                .then().statusCode(403);
    }

    @Test
    void criarAulaSemAutenticacaoRetorna401() {
        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": 1, "professorId": 1, "horarioId": 1, "vagas": 30}
                        """)
                .when().post("/aulas")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void criarAulaComProfessorConflitanteRetorna409() {
        Long disciplina = seeder.disciplinaId("Matemática");
        Long professor = seeder.professorId("Ana Paula");
        Long horario = seeder.horarioId("Segunda", "08:00", "10:00");
        criarAulaComoCoordenador(disciplina, professor, horario, 30);

        // Segundo professor não existe; mesma dupla professor/horário conflita.
        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 20}
                        """.formatted(disciplina, professor, horario))
                .when().post("/aulas")
                .then()
                .statusCode(409)
                .body("code", equalTo("business_error"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void criarAulaComProfessorQueNaoLecionaDisciplinaRetorna400() {
        // Carlos Alberto leciona Português, não Matemática.
        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 30}
                        """.formatted(
                        seeder.disciplinaId("Matemática"),
                        seeder.professorId("Carlos Alberto"),
                        seeder.horarioId("Segunda", "08:00", "10:00")))
                .when().post("/aulas")
                .then()
                .statusCode(400)
                .body("code", equalTo("invalid_argument"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void listarComFiltrosRetornaSomenteAulasCorrespondentes() {
        criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 30);
        criarAulaComoCoordenador(
                seeder.disciplinaId("Português"), seeder.professorId("Carlos Alberto"),
                seeder.horarioId("Terça", "08:00", "10:00"), 25);

        given()
                .queryParam("professorId", seeder.professorId("Ana Paula"))
                .when().get("/aulas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].professorNome", equalTo("Ana Paula"));

        given()
                .queryParam("diaSemana", "segunda")
                .when().get("/aulas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].horarioDiaSemana", equalTo("Segunda"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void listarPorCursoAutorizadoRetornaSomenteAulasDaqueleCurso() {
        Long computacao = seeder.cursoId("Ciência da Computação");
        Long engenharia = seeder.cursoId("Engenharia Civil");

        // Aula autorizada só para Computação.
        given().contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 30, "cursoIds": [%d]}
                        """.formatted(
                        seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                        seeder.horarioId("Segunda", "08:00", "10:00"), computacao))
                .when().post("/aulas").then().statusCode(201);

        // Aula autorizada só para Engenharia.
        given().contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 20, "cursoIds": [%d]}
                        """.formatted(
                        seeder.disciplinaId("Português"), seeder.professorId("Carlos Alberto"),
                        seeder.horarioId("Terça", "08:00", "10:00"), engenharia))
                .when().post("/aulas").then().statusCode(201);

        given()
                .queryParam("cursoId", computacao)
                .when().get("/aulas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].disciplinaNome", equalTo("Matemática"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void listarPorVagasDisponiveisRetornaSomenteAulasComVaga() {
        Long aulaCheia = criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 1);
        matriculaService.matricular(seeder.alunoId("aluno1@email.com"), aulaCheia);
        criarAulaComoCoordenador(
                seeder.disciplinaId("Português"), seeder.professorId("Carlos Alberto"),
                seeder.horarioId("Terça", "08:00", "10:00"), 25);

        given()
                .queryParam("vagasDisponiveis", true)
                .when().get("/aulas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].disciplinaNome", equalTo("Português"));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void buscarAulaInexistenteRetorna404() {
        given()
                .when().get("/aulas/999999")
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void atualizarVagasAbaixoDosMatriculadosRetorna400() {
        Long aulaId = criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 5);

        matriculaService.matricular(seeder.alunoId("aluno1@email.com"), aulaId);
        matriculaService.matricular(seeder.alunoId("aluno2@email.com"), aulaId);

        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 1}
                        """.formatted(
                        seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                        seeder.horarioId("Segunda", "08:00", "10:00")))
                .when().put("/aulas/" + aulaId)
                .then()
                .statusCode(400);
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void atualizarAulaMantemOcupacaoNaResposta() {
        Long aulaId = criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 3);
        matriculaService.matricular(seeder.alunoId("aluno1@email.com"), aulaId);

        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 3}
                        """.formatted(
                        seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                        seeder.horarioId("Segunda", "08:00", "10:00")))
                .when().put("/aulas/" + aulaId)
                .then()
                .statusCode(200)
                .body("vagasOcupadas", equalTo(1))
                .body("vagasRestantes", equalTo(2));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void excluirAulaComMatriculasRetorna409() {
        Long aulaId = criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 5);
        matriculaService.matricular(seeder.alunoId("aluno1@email.com"), aulaId);

        given()
                .when().delete("/aulas/" + aulaId)
                .then()
                .statusCode(409)
                .body("code", equalTo("illegal_state"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void excluirAulaSemMatriculasRetorna204() {
        Long aulaId = criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 5);

        given()
                .when().delete("/aulas/" + aulaId)
                .then().statusCode(204);
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void exclusaoLogicaRemoveDaListagemEBusca() {
        Long aulaId = criarAulaComoCoordenador(
                seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                seeder.horarioId("Segunda", "08:00", "10:00"), 5);

        given().when().delete("/aulas/" + aulaId).then().statusCode(204);

        // A aula excluída não aparece na listagem nem é buscável por id.
        given().when().get("/aulas").then()
                .statusCode(200)
                .body("size()", equalTo(0));

        given().when().get("/aulas/" + aulaId).then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    @TestSecurity(user = "coordenador2@email.com", roles = "coordenador")
    void coordenadorNaoAcessaNemAtualizaAulaDeOutro() {
        // Cria uma aula pertencente ao coordenador1 (chamada direta ao serviço).
        AulaRequest request = new AulaRequest();
        request.setDisciplinaId(seeder.disciplinaId("Matemática"));
        request.setProfessorId(seeder.professorId("Ana Paula"));
        request.setHorarioId(seeder.horarioId("Segunda", "08:00", "10:00"));
        request.setVagas(5);
        Long aulaId = aulaService.criar(request, seeder.coordenadorId("coordenador1@email.com")).getId();

        // Autenticado como coordenador2: não vê nem atualiza a aula de outro.
        given()
                .when().get("/aulas/" + aulaId)
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));

        given()
                .contentType("application/json")
                .body("""
                        {"disciplinaId": %d, "professorId": %d, "horarioId": %d, "vagas": 9}
                        """.formatted(
                        seeder.disciplinaId("Matemática"), seeder.professorId("Ana Paula"),
                        seeder.horarioId("Segunda", "08:00", "10:00")))
                .when().put("/aulas/" + aulaId)
                .then()
                .statusCode(404);
    }
}
