package org.country;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CountryTestJdbc {
    RequestSpecification request;
    Response responseAuthorization;
    Connection connection;
    Response responsePost;

    private final String URI = "http://31.131.249.140";
    private final int PORT = 8080;
    private final String PATH = "/api/";
    private String token;

    private final String URL = "jdbc:postgresql://31.131.249.140:5432/app-db";
    private final String USER = "app-db-admin";
    private final String PASSWORD = "AiIoqv6c2k0gVhx2";


    @BeforeEach
    public void setConnect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

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
    }

    @AfterEach
    public void closeConnect() throws SQLException {
        connection.close();
    }

    @Test
    @DisplayName("Проверка сценария GET countries")
    public void shouldGAllCountries() throws SQLException {
        int newCountryId;
        try (
                final PreparedStatement newCountry = connection.prepareStatement("INSERT INTO COUNTRY(ID,COUNTRY_NAME) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            newCountry.setInt(1, 6666);
            newCountry.setString(2, "Польша");

            assumeTrue(newCountry.executeUpdate() == 1);

            try (final ResultSet generatedKeys = newCountry.getGeneratedKeys()) {
                assumeTrue(generatedKeys.next());
                newCountryId = generatedKeys.getInt(1);
            }
        }

        int countCountry;
        try (
                final PreparedStatement countCountries = connection.prepareStatement("SELECT COUNT(*) FROM COUNTRY");
                final ResultSet resultSet = countCountries.executeQuery()) {
            assumeTrue(resultSet.next());
            countCountry = resultSet.getInt(1);
        }

        try {
            request.when()
                    .get("/countries")
                    .then()
                    .statusCode(SC_OK)
                    .body("size()", is(countCountry), "id", hasItem(newCountryId));
        } finally {
            try (final PreparedStatement deleteRegion = connection.prepareStatement("DELETE FROM COUNTRY WHERE ID=?")) {
                deleteRegion.setInt(1, newCountryId);
                assumeTrue(deleteRegion.executeUpdate() == 1);
            }
        }
    }

    @Test
    @DisplayName("Проверка сценария POST countries с JDBC")
    public void shouldPostCountries() throws SQLException {
        responsePost = request
                .when()
                .body("{\n" +
                        " \"countryName\": \"Austria\"\n" +
                        "}")
                .post("/countries");

        int countryIdBd;

        try (
                final PreparedStatement getCountryId = connection.prepareStatement("SELECT ID FROM COUNTRY WHERE COUNTRY_NAME=?")) {
            getCountryId.setString(1, "Austria");
            final ResultSet resultSet = getCountryId.executeQuery();
            assumeTrue(resultSet.next());
            countryIdBd = resultSet.getInt(1);
        }
        int countryIdPost;

        try {
            countryIdPost = responsePost.jsonPath().getInt("id");
            assertEquals(countryIdPost, countryIdBd);
        } finally {
            try (final PreparedStatement deleteRegion = connection.prepareStatement("DELETE FROM COUNTRY WHERE ID=?")) {
                deleteRegion.setInt(1, countryIdBd);
                assumeTrue(deleteRegion.executeUpdate() == 1);
            }
        }
    }
}
