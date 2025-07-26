package org.example;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TariffApiClientTest {

    // Postman ile çalıştığını söylediğin endpoint:
    private static final String BASE_URL = "http://34.123.86.69/api/v1/balance";

    @Test
    public void testValidNumberReturnsTariffInfo() {
        String jsonBody = "{\"msisdn\": \"05129478360\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(BASE_URL)
                .then()
                .log().all()
                .statusCode(200)
                .body("packageName", not(emptyString()))
                .body("price", greaterThan(0f))
                .body("remainingMinutes", greaterThanOrEqualTo(0))
                .body("remainingData", greaterThanOrEqualTo(0))
                .body("remainingSms", greaterThanOrEqualTo(0));
    }

    @Test
    public void testUnregisteredNumberReturnsError() {
        String jsonBody = "{\"msisdn\": \"00000000000\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(BASE_URL)
                .then()
                .log().all()
                .statusCode(anyOf(is(400), is(500)))
                .body("message", anyOf(nullValue(), containsString("Numara sisteme kayıtlı değil")));
    }

    @Test
    public void testInvalidFormatNumberReturnsBadRequest() {
        String jsonBody = "{\"msisdn\": \"abc123\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(BASE_URL)
                .then()
                .log().all()
                .statusCode(anyOf(is(400), is(500))); // Şimdilik 400 veya 500 olabilir
    }

    @Test
    public void testEmptyNumberReturnsBadRequest() {
        String jsonBody = "{\"msisdn\": \"\"}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(BASE_URL)
                .then()
                .log().all()
                .statusCode(anyOf(is(400), is(500))) // 400 veya 500 kabul et
                .body("error", notNullValue());
    }
}
