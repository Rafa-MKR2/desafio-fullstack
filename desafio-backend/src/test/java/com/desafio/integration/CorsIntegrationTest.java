package com.desafio.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * Garante que o filtro CORS esteja realmente ativo (a chave antiga
 * quarkus.http.cors era ignorada silenciosamente no boot) e reflita a
 * origem permitida configurada para o Angular (http://localhost:4200).
 */
@QuarkusTest
class CorsIntegrationTest {

    @Test
    void preflightDeOrigemPermitidaRetorna200ComHeadersCors() {
        given()
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET")
                .when().options("/aulas")
                .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", equalTo("http://localhost:4200"))
                .header("Access-Control-Allow-Methods", containsString("GET"));
    }

    @Test
    void preflightDeOrigemNaoPermitidaRetorna403() {
        given()
                .header("Origin", "http://origem-maliciosa.example.com")
                .header("Access-Control-Request-Method", "GET")
                .when().options("/aulas")
                .then()
                .statusCode(403);
    }
}
