import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("api")
public class ReqresTests extends TestBase {

    @DisplayName("Тестирование запроса Get List Users c queryParams page")
    @Test
    void getListUsersTest() {

        given()
                .when()
                .queryParam("page", 1)
                .get("api/users")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .assertThat()
                .body(matchesJsonSchemaInClasspath("ListUsersSchema.json"));
    }

    @DisplayName("Тестирование запроса Get получить пользователя по его id")
    @Test
    void getSingleUserByIdTest() throws Exception {

        String expectedResult = Files.readString(Paths.get("src/test/resources/SingleUserJson.json"));

        String responseBody = given()
                .when()
                .get("api/users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().asString();

        assertEquals(expectedResult, responseBody);
    }

    @DisplayName("Тестирование запроса Post c проверкой key/value по полям name, job")
    @Test
    void createNewUserTest() {

        File jsonTemplate = new File("src/test/resources/UserTemplate.json");

        given()
                .when()
                .contentType(JSON)
                .body(jsonTemplate)
                .post("api/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body(matchesJsonSchemaInClasspath("CreateUserSchema.json"))
                .body("name", is("igafarov"))
                .body("job", is("QA"));
    }


    @Test
    @DisplayName("Тестирование запроса Put c обновлением данных Users по полю job")
    public void modifyUser() {

        File jsonTemplate = new File("src/test/resources/UserTemplate.json");

        String id = given()
                .when()
                .contentType(JSON)
                .body(jsonTemplate)
                .post("api/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response()
                .body()
                .path("id");

        String json = "{\"name\": \"igafarov\",\"job\": \"DEV\"}";

        given()
                .when()
                .contentType(JSON)
                .body(json)
                .put("/api/users/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", is("igafarov"))
                .body("job", is("DEV"))
                .body(matchesJsonSchemaInClasspath("UpdateUserSchema.json"));

    }

    @Test
    @DisplayName("Тестирование запроса Delete c удалением пользователя")
    public void deleteUser() {

        File jsonTemplate = new File("src/test/resources/UserTemplate.json");

        String id = given()
                .when()
                .contentType(JSON)
                .body(jsonTemplate)
                .post("api/users")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response()
                .body()
                .path("id");

        given()
                .when()
                .delete("/api/users/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }
}
