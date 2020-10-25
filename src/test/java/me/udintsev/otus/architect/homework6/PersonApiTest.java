package me.udintsev.otus.architect.homework6;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;


@SpringBootTest(classes = DatabaseConfig.class)
@AutoConfigureWebTestClient
public class PersonApiTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void shouldInsertAndListUsers() {
        webTestClient.post()
                .uri("/person")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "first": "SomeFirst",
                          "last": "SomeLast"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.first").isEqualTo("SomeFirst")
                .jsonPath("$.last").isEqualTo("SomeLast");

        webTestClient.get()
                .uri("/person")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isNumber()
                .jsonPath("$[0].first").isEqualTo("SomeFirst")
                .jsonPath("$[0].last").isEqualTo("SomeLast");
    }
}
