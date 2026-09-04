package com.desafio.integration;

import com.desafio.dto.AulaRequest;
import com.desafio.entity.Aula;
import com.desafio.exception.VagasEsgotadasException;
import com.desafio.service.AulaService;
import com.desafio.service.MatriculaService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class MatriculaResourceIntegrationTest {

    @Inject
    TestDataSeeder seeder;

    @Inject
    AulaService aulaService;

    @Inject
    MatriculaService matriculaService;

    @BeforeEach
    void setUp() {
        seeder.resetarBase();
    }

    private Long criarAula(String disciplina, String professor, String dia, int vagas) {
        AulaRequest request = new AulaRequest();
        request.setDisciplinaId(seeder.disciplinaId(disciplina));
        request.setProfessorId(seeder.professorId(professor));
        request.setHorarioId(seeder.horarioId(dia, "08:00", "10:00"));
        request.setVagas(vagas);
        return aulaService.criar(request).getId();
    }

    private Long criarAulaSobreposta(String disciplina, String professor, int vagas) {
        AulaRequest request = new AulaRequest();
        request.setDisciplinaId(seeder.disciplinaId(disciplina));
        request.setProfessorId(seeder.professorId(professor));
        // Segunda 09:00-11:00 cruza com 08:00-10:00.
        request.setHorarioId(seeder.horarioId("Segunda", "09:00", "11:00"));
        request.setVagas(vagas);
        return aulaService.criar(request).getId();
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void matricularComSucessoRetorna201() {
        Long aulaId = criarAula("Matemática", "Ana Paula", "Segunda", 10);

        given()
                .contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaId))
                .when().post("/matriculas")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("aulaId", equalTo(aulaId.intValue()));

        // A ocupação da aula deve refletir a matrícula recém-criada.
        given()
                .when().get("/aulas/" + aulaId)
                .then()
                .statusCode(200)
                .body("vagasOcupadas", equalTo(1))
                .body("vagasRestantes", equalTo(9));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void matriculaDuplicadaRetorna409() {
        Long aulaId = criarAula("Matemática", "Ana Paula", "Segunda", 10);
        String body = """
                {"aulaId": %d}
                """.formatted(aulaId);

        given().contentType("application/json").body(body).when().post("/matriculas")
                .then().statusCode(201);
        given().contentType("application/json").body(body).when().post("/matriculas")
                .then()
                .statusCode(409)
                .body("code", equalTo("business_error"));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void matriculaEmAulaInexistenteRetorna404() {
        given()
                .contentType("application/json")
                .body("""
                        {"aulaId": 999999}
                        """)
                .when().post("/matriculas")
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void matriculaSemVagasLancaVagasEsgotadas() {
        Long aulaId = criarAula("Matemática", "Ana Paula", "Segunda", 1);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaId))
                .when().post("/matriculas")
                .then().statusCode(201);

        // Segundo aluno tenta a única vaga já ocupada (chamada direta, pois a
        // identidade @TestSecurity é fixa por método).
        assertThrows(VagasEsgotadasException.class,
                () -> matriculaService.matricular(seeder.alunoId("aluno2@email.com"), aulaId));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void matriculaComChoqueDeHorarioRetorna409() {
        Long aulaManha = criarAula("Matemática", "Ana Paula", "Segunda", 10);
        Long aulaSobreposta = criarAulaSobreposta("Português", "Carlos Alberto", 10);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaManha))
                .when().post("/matriculas")
                .then().statusCode(201);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaSobreposta))
                .when().post("/matriculas")
                .then()
                .statusCode(409)
                .body("code", equalTo("business_error"));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void matriculaEmDiaDiferenteNaoGeraConflito() {
        Long aulaSegunda = criarAula("Matemática", "Ana Paula", "Segunda", 10);

        AulaRequest request = new AulaRequest();
        request.setDisciplinaId(seeder.disciplinaId("Português"));
        request.setProfessorId(seeder.professorId("Carlos Alberto"));
        request.setHorarioId(seeder.horarioId("Terça", "08:00", "10:00"));
        request.setVagas(10);
        Aula aulaTerca = aulaService.criar(request);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaSegunda))
                .when().post("/matriculas")
                .then().statusCode(201);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaTerca.getId()))
                .when().post("/matriculas")
                .then().statusCode(201);
    }

    @Test
    @TestSecurity(user = "aluno-sem-cadastro@email.com", roles = "aluno")
    void alunoSemCadastroNoBancoRetorna404() {
        Long aulaId = criarAula("Matemática", "Ana Paula", "Segunda", 10);

        given()
                .contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaId))
                .when().post("/matriculas")
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void coordenadorNaoPodeMatricularRetorna403() {
        Long aulaId = criarAula("Matemática", "Ana Paula", "Segunda", 10);

        given()
                .contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaId))
                .when().post("/matriculas")
                .then().statusCode(403);
    }

    @Test
    void listarMatriculasSemAutenticacaoRetorna401() {
        given()
                .when().get("/matriculas")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void listarMatriculasDoAlunoSemMatriculasRetornaListaVazia() {
        given()
                .when().get("/matriculas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void listarMatriculasDoAlunoRetornaSomenteAsDele() {
        Long aulaSegunda = criarAula("Matemática", "Ana Paula", "Segunda", 10);
        Long aulaTerca = criarAula("Português", "Carlos Alberto", "Terça", 10);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaSegunda))
                .when().post("/matriculas")
                .then().statusCode(201);
        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaTerca))
                .when().post("/matriculas")
                .then().statusCode(201);

        // Outro aluno se matricula na mesma aula: as listagens do aluno1
        // autenticado não podem vazar os dados do aluno2.
        matriculaService.matricular(seeder.alunoId("aluno2@email.com"), aulaSegunda);

        given()
                .when().get("/matriculas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("aulaId", hasItems(aulaSegunda.intValue(), aulaTerca.intValue()))
                .body("alunoId", everyItem(equalTo(seeder.alunoId("aluno1@email.com").intValue())));

        given()
                .when().get("/matriculas/aulas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("id", hasItems(aulaSegunda.intValue(), aulaTerca.intValue()));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void listarAulasDoAlunoRetornaAulasComOcupacao() {
        Long aulaId = criarAula("Matemática", "Ana Paula", "Segunda", 10);

        given().contentType("application/json")
                .body("""
                        {"aulaId": %d}
                        """.formatted(aulaId))
                .when().post("/matriculas")
                .then().statusCode(201);

        given()
                .when().get("/matriculas/aulas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(aulaId.intValue()))
                .body("[0].disciplinaNome", equalTo("Matemática"))
                .body("[0].professorNome", equalTo("Ana Paula"))
                .body("[0].vagas", equalTo(10))
                .body("[0].vagasOcupadas", equalTo(1))
                .body("[0].vagasRestantes", equalTo(9));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void coordenadorNaoPodeListarMatriculasRetorna403() {
        given()
                .when().get("/matriculas")
                .then().statusCode(403);

        given()
                .when().get("/matriculas/aulas")
                .then().statusCode(403);
    }
}
