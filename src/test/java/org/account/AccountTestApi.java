package org.account;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

public class AccountTestApi {
    RequestSpecification request;

    @BeforeEach
    public void init() {
        request =
                given()
                        .baseUri("http://31.131.249.140")
                        .port(8080)
                        .basePath("/api")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTYzNTM1MDkxNH0.wR-tT7e2iZWm2R892VFzt--micaRk8XPeh9Sdx55GGvletZTICnJHVw19mex_IgUDgM5ysjDzIkHyA1k3OYN9w")
                        .contentType(ContentType.JSON);
    }


    @Test
    @DisplayName("Проверка сценария GET account-resource")
    public void shouldGetAccountResource() {
        request.when()
                .get("/account")
                .then()
                .statusCode(SC_OK)
                .body("size()", is(13), "id", is(1));
    }
}

