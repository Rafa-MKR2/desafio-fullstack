package com.desafio.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

@QuarkusTest
class CatalogoResourceIntegrationTest {

    @Inject
    TestDataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder.resetarBase();
    }

    @Test
    void listarCatalogoSemAutenticacaoRetorna401() {
        given()
                .when().get("/disciplinas")
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void alunoListaDisciplinas() {
        given()
                .when().get("/disciplinas")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("nome", hasItems("Matemática", "Português", "História"));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void alunoListaProfessores() {
        given()
                .when().get("/professores")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("nome", hasItems("Ana Paula", "Carlos Alberto"));
    }

    @Test
    @TestSecurity(user = "coordenador1@email.com", roles = "coordenador")
    void coordenadorListaHorarios() {
        given()
                .when().get("/horarios")
                .then()
                .statusCode(200)
                .body("size()", equalTo(4))
                .body("diaSemana", hasItems("Segunda", "Terça", "Quarta"));
    }

    @Test
    @TestSecurity(user = "aluno1@email.com", roles = "aluno")
    void alunoListaCursos() {
        given()
                .when().get("/cursos")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("nome", hasItems("Ciência da Computação", "Engenharia Civil"));
    }
}
