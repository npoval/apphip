package org.account;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.given;

import static org.apache.http.HttpStatus.SC_OK;


public class AccountTestApi {
    RequestSpecification request;
    Response responseAuthorization;

    private final String URI = "http://31.131.249.140";
    private final int PORT = 8080;
    private final String PATH = "/api/";
    private String token;


    @BeforeEach
    public void init() {
        responseAuthorization = given()
                .baseUri(URI)
                .port(PORT)
                .basePath(PATH)
                .contentType(ContentType.JSON)
                .when()
                .body("{\n" +
                        "  \"password\": \"kMBc3Lb7iM3sd0Mt\",\n" +
                        "  \"rememberMe\": true,\n" +
                        "  \"username\": \"user\"\n" +
                        "}")
                .post("/authenticate");

        token = responseAuthorization.jsonPath().get("id_token");

        request =
                given()
                        .baseUri("http://31.131.249.140")
                        .port(8080)
                        .basePath("/api")
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Проверка сценария GET account-resource")
    public void shouldGetAccountResource() {
        request.when()
                .get("/account")
                .then()
                .statusCode(SC_OK);
    }
}

