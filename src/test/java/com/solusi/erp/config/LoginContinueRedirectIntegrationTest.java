package com.solusi.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
class LoginContinueRedirectIntegrationTest {

    private static final Pattern CSRF_PATTERN = Pattern.compile(
            "name=\"_csrf\"\\s+value=\"([^\"]+)\"|value=\"([^\"]+)\"\\s+name=\"_csrf\"");

    @LocalServerPort
    private int port;

    @Test
    void staleSessionProtectedRequestContinuesToOriginalUrlAfterLogin() throws Exception {
        HttpClient client = newClient();

        HttpResponse<String> protectedResponse = get(client, "/purchasing/purchase-orders", "JSESSIONID=stale-session-id");

        assertThat(protectedResponse.statusCode()).isEqualTo(302);
        assertThat(protectedResponse.headers().firstValue("location")).hasValueSatisfying(location ->
                assertThat(location).endsWith("/login"));

        HttpResponse<String> loginPage = get(client, "/login", null);
        String csrfToken = extractCsrfToken(loginPage.body());

        HttpResponse<String> loginResponse = postLogin(client, csrfToken);

        assertThat(loginResponse.statusCode()).isEqualTo(302);
        assertThat(loginResponse.headers().firstValue("location")).hasValueSatisfying(location ->
                assertThat(URI.create(location).getPath()).isEqualTo("/purchasing/purchase-orders"));
    }

    private HttpClient newClient() {
        CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        return HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private HttpResponse<String> get(HttpClient client, String path, String cookieHeader)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).GET();
        if (cookieHeader != null) {
            builder.header("Cookie", cookieHeader);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postLogin(HttpClient client, String csrfToken) throws IOException, InterruptedException {
        String form = formValue("username", "admin")
                + "&" + formValue("password", "admin123")
                + "&" + formValue("_csrf", csrfToken);
        HttpRequest request = HttpRequest.newBuilder(uri("/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private String extractCsrfToken(String html) {
        Matcher matcher = CSRF_PATTERN.matcher(html);
        assertThat(matcher.find()).as("login page should contain CSRF token").isTrue();
        return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }

    private String formValue(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
