import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PatientIntegrationTest {

    private static String accessToken;

    @BeforeAll
    static void setUp() {
        // Set base URI to API Gateway port
        RestAssured.baseURI = "http://localhost:4004";

        // Login payload uses the credentials (make sure this user exists in your AuthService DB)
        String loginPayload = """
                {
                  "email":"testuser@test.com",
                  "password":"password123"
                }
                """;

        System.out.println("--- Attempting to login via /auth/login ---");

        // Login to get access token (This verifies the auth route is working)
        Response loginResponse = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract()
                .response();

        accessToken = loginResponse.jsonPath().getString("accessToken");
        System.out.println("Access token successfully extracted: " + accessToken.substring(0, 20) + "...");
        System.out.println("----------------------------------------------");
    }

    /**
     * Test case to ensure the API Gateway successfully routes the request
     * and the JwtValidation filter permits the request with a valid token.
     * The path is corrected from /patients to /api/patients based on the Gateway configuration.
     */
    @Test
    public void shouldReturnAllPatientsWithValidToken() {
        System.out.println("--- Testing access to /api/patients with VALID token ---");
        given()
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/api/patients") // Path corrected here!
                .then()
                .statusCode(200)
                .body("$", not(empty())); // Verifies that the response array is not empty
        System.out.println("Patient data retrieved successfully (200 OK).");
    }

    /**
     * Ensure the JwtValidation filter blocks requests when NO token is provided.
     */
    @Test
    public void shouldBeRejectedWithoutToken() {
        System.out.println("--- Testing access to /api/patients WITHOUT token ---");
        given()
                // No Authorization header provided
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401); // Expecting Unauthorized status
        System.out.println("Request blocked successfully (401 Unauthorized).");
    }

    /**
     * Add a test case to ensure the JwtValidation filter blocks requests
     * when an invalid or malformed token is provided.
     */
    @Test
    public void shouldBeRejectedWithInvalidToken() {
        final String invalidToken = "Bearer this.is.an.invalid.token";
        System.out.println("--- Testing access to /api/patients with INVALID token ---");
        given()
                .header("Authorization", invalidToken)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(401); // Expecting Unauthorized status
        System.out.println("Request blocked successfully with bad token (401 Unauthorized).");
    }
}
