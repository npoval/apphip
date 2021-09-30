package org.region;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class RegionTestApi {
    private RequestSpecification request;
    private Response responseAuthorization;
    private Response response;
    private RequestSpecification requestForDelete;


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
                        .baseUri(URI)
                        .port(PORT)
                        .basePath(PATH)
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON);

        requestForDelete = given()
                .baseUri(URI)
                .port(PORT)
                .basePath(PATH)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON);
    }

    @Test
    @DisplayName("Создание региона")
    public void shouldPostRegions() {
        response = request.when()
                .body("{\n" +
                        " \"regionName\": \"Краснодар\"\n" +
                        "}")
                .post("/regions");
        response.then().statusCode(HttpStatus.SC_CREATED).body("regionName", is("Краснодар"));
        int idRegion = response.jsonPath().getInt("id");

        request.when()
                .delete("/regions/{id}", idRegion)
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("Проверка возврата региона по id")
    public void shouldGetRegionById() {
        response = request.when()
                .body("{\n" +
                        " \"regionName\": \"Новосибирск2\"\n" +
                        "}")
                .post("/regions");
        response.then().statusCode(HttpStatus.SC_CREATED)
                .body("regionName", is("Новосибирск2"));

        int idRegion = response.jsonPath().getInt("id");

        request.when()
                .get("/regions/{id}", idRegion)
                .then().statusCode(HttpStatus.SC_OK)
                .body("regionName", is("Новосибирск2"));

        requestForDelete
                .delete("/regions/{id}", idRegion)
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("Проверка обновления региона")
    public void shouldPutRegion() {
        response = request.when()
                .body("{\n" +
                        " \"regionName\": \"Анапа\"\n" +
                        "}")
                .post("/regions");
        response.then().statusCode(HttpStatus.SC_CREATED);

        int idRegion = response.jsonPath().getInt("id");

        request.when()
                .body("{\n" +
                        "  \"id\": " + idRegion + ",\n" +
                        "  \"regionName\": \"Анапа2\"\n" +
                        "}")
                .put("/regions/{id}", idRegion)
                .then().statusCode(HttpStatus.SC_OK).body("regionName", is("Анапа2"));

        requestForDelete
                .delete("/regions/{id}", idRegion)
                .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }
}

