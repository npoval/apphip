package org.department;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;

import java.sql.*;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DepartmentTestJdbc {
    private RequestSpecification request;
    private Response responseAuthorization;
    private Connection connection;
    private Response responsePost;

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
    @DisplayName("Проверка сценария POST Departments")
    public void shouldPostDepartments() throws SQLException {
        int newRegionId;
        try (
                final PreparedStatement newRegion = connection.prepareStatement("INSERT INTO REGION(ID,REGION_NAME) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
            newRegion.setInt(1, 66000);
            newRegion.setString(2, "Калужский");

            assumeTrue(newRegion.executeUpdate() == 1);

            try (final ResultSet generatedKeys = newRegion.getGeneratedKeys()) {
                assumeTrue(generatedKeys.next());
                newRegionId = generatedKeys.getInt(1);
            }
        }

        int newCountryId;
        try (
                final PreparedStatement newCountry = connection.prepareStatement("INSERT INTO COUNTRY(ID,COUNTRY_NAME, REGION_ID) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            newCountry.setInt(1, 66667);
            newCountry.setString(2, "Россия");
            newCountry.setInt(3, newRegionId);

            assumeTrue(newCountry.executeUpdate() == 1);

            try (final ResultSet generatedKeys = newCountry.getGeneratedKeys()) {
                assumeTrue(generatedKeys.next());
                newCountryId = generatedKeys.getInt(1);
            }
        }

        int newLocationId;
        try (
                final PreparedStatement newLocation = connection.prepareStatement("INSERT INTO LOCATION(ID,CITY,COUNTRY_ID, POSTAL_CODE,PROVINCE, STREET_ADDRESS) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            newLocation.setInt(1, 6101010);
            newLocation.setString(2, "Калуга");
            newLocation.setInt(3, newCountryId);
            newLocation.setString(4, "456123");
            newLocation.setString(5, "Село Красное");
            newLocation.setString(6, "Ленина, 23");

            assumeTrue(newLocation.executeUpdate() == 1);

            try (final ResultSet generatedKeys = newLocation.getGeneratedKeys()) {
                assumeTrue(generatedKeys.next());
                newLocationId = generatedKeys.getInt(1);
            }
        }


        int idDep =0;
        try {
            responsePost = request
                    .when()
                    .body("{\"departmentName\": \"ПрогТест\",\n" +
                            "  \"location\": {\n" +
                            "    \"id\": "+newLocationId+" \n" +
                            " }}")
                    .post("/departments");
            responsePost.then().statusCode(SC_CREATED).body("departmentName", is("ПрогТест"));
            idDep = responsePost.jsonPath().getInt("id");
        }

        finally {
            try (final PreparedStatement deleteDepartment = connection.prepareStatement("DELETE FROM DEPARTMENT WHERE ID=?")) {
                deleteDepartment.setInt(1, idDep);
                assumeTrue(deleteDepartment.executeUpdate() == 1);
            }
            try (final PreparedStatement deleteLocation = connection.prepareStatement("DELETE FROM LOCATION WHERE ID=?")) {
                deleteLocation.setInt(1, newLocationId);
                assumeTrue(deleteLocation.executeUpdate() == 1);
            }

            try (final PreparedStatement deleteCountry = connection.prepareStatement("DELETE FROM COUNTRY WHERE ID=?")) {
                deleteCountry.setInt(1, newCountryId);
                assumeTrue(deleteCountry.executeUpdate() == 1);
            }

            try (final PreparedStatement deleteRegion = connection.prepareStatement("DELETE FROM REGION WHERE ID=?")) {
                deleteRegion.setInt(1, newRegionId);
                assumeTrue(deleteRegion.executeUpdate() == 1);
            }
        }
    }
}
