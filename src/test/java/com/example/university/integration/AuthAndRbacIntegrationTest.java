package com.example.university.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")   // application-test.properties use hobe
class AuthAndRbacIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    // ---------- helpers ----------

    private HttpResponse<String> send(String method, String path, String token, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest.BodyPublisher body = (jsonBody == null)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(jsonBody);
        return client.send(builder.method(method, body).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String username, String password) throws Exception {
        HttpResponse<String> res = send("POST", "/api/auth/login", null,
                "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}");
        assertThat(res.statusCode()).isEqualTo(200);

        Matcher m = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(res.body());
        assertThat(m.find()).isTrue();
        return m.group(1);
    }

    // tomar API te role body er bhetore jay
    private String teacherJson(String username) {
        return "{\"username\":\"" + username + "\",\"password\":\"123456\",\"role\":\"TEACHER\","
                + "\"fullName\":\"Teacher\",\"email\":\"" + username + "@uni.edu\","
                + "\"phone\":\"0170000000\",\"department\":\"CSE\"}";
    }

    // ---------- tests ----------

    // Test 7
    @Test
    void login_withValidCredentials_returnsJwtToken() throws Exception {
        HttpResponse<String> res = send("POST", "/api/auth/login", null,
                "{\"username\":\"admin\",\"password\":\"admin123\"}");

        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.body()).contains("\"token\"").contains("ADMIN");
    }

    // Test 8
    @Test
    void admin_canCreateTeacherAndSeeItInList() throws Exception {
        String adminToken = login("admin", "admin123");

        HttpResponse<String> created = send("POST", "/api/admin/users", adminToken, teacherJson("it_teacher1"));
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.body()).doesNotContain("password");   // password response e ashe na

        HttpResponse<String> list = send("GET", "/api/admin/users?role=TEACHER", adminToken, null);
        assertThat(list.statusCode()).isEqualTo(200);
        assertThat(list.body()).contains("it_teacher1");
    }

    // Test 9
    @Test
    void teacher_isForbiddenOnAdminApi_canReadOwnProfile_andAnonymousIsUnauthorized() throws Exception {
        String adminToken = login("admin", "admin123");
        HttpResponse<String> created = send("POST", "/api/admin/users", adminToken, teacherJson("it_teacher2"));
        assertThat(created.statusCode()).isEqualTo(201);

        String teacherToken = login("it_teacher2", "123456");

        HttpResponse<String> me = send("GET", "/api/me", teacherToken, null);
        assertThat(me.statusCode()).isEqualTo(200);                                              // nijer profile
        assertThat(me.body()).contains("it_teacher2");

        assertThat(send("GET", "/api/admin/users", teacherToken, null).statusCode()).isEqualTo(403);   // admin API te teacher na
        assertThat(send("GET", "/api/admin/users", null, null).statusCode()).isEqualTo(401);           // token nai
    }
}