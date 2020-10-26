package me.udintsev.otus.architect.homework6.person;

import me.udintsev.otus.architect.homework6.DatabaseConfig;
import me.udintsev.otus.architect.homework6.RestDocsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;


@SpringBootTest(classes = {DatabaseConfig.class, RestDocsConfig.class})
@AutoConfigureWebTestClient
@AutoConfigureRestDocs
// @Transactional doesn't work here: https://github.com/spring-projects/spring-framework/issues/24226
// Because of that we don't test things in isolation, but group everything to a single test scenario
public class PersonApiTest {
    private static final String SOME_FIRST_NAME = "John";
    private static final String SOME_LAST_NAME = "Doe";

    private static final String OTHER_FIRST_NAME = "Jane";
    private static final String OTHER_LAST_NAME = "Dane";

    private static final FieldDescriptor[] PERSON_DESCRIPTOR = new FieldDescriptor[]{
            fieldWithPath("id").description("ID of the person"),
            fieldWithPath("first").description("Person first name"),
            fieldWithPath("last").description("Person last name")
    };

    private static final PathParametersSnippet ID_PATH_PARAMETERS_DESCRIPTOR = pathParameters(
            parameterWithName("id").description("ID of the person")
    );


    @Autowired
    WebTestClient webTestClient;

    @Autowired
    PersonService personService;

    @Test
    void createAndList() {
        String location;
        long id;

        // Create
        {
            List<String> locationHolder = new ArrayList<>(1);
            List<Long> idHolder = new ArrayList<>(1);

            webTestClient.post()
                    .uri("/person")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {
                              "first": "%s",
                              "last": "%s"
                            }
                            """.formatted(SOME_FIRST_NAME, SOME_LAST_NAME))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectHeader()
                    .value("location",
                            l -> {
                                locationHolder.add(l);
                                assertThat(l).startsWith("/person/");
                                assertThat(l).hasSizeGreaterThan("/person/".length());
                            })
                    .expectBody()
                    .jsonPath("$.id").isNumber()
                    .jsonPath("$.id").value(idHolder::add, Long.class)
                    .jsonPath("$.first").isEqualTo(SOME_FIRST_NAME)
                    .jsonPath("$.last").isEqualTo(SOME_LAST_NAME)
                    .consumeWith(document("create",
                            requestFields(
                                    fieldWithPath("first").description("First name of the person to create"),
                                    fieldWithPath("last").description("Last name of the person to create")
                            ),
                            responseFields(PERSON_DESCRIPTOR)
                    ));

            location = locationHolder.get(0);
            id = idHolder.get(0);
        }

        // Get by ID
        webTestClient.get()
                .uri("/person/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.first").isEqualTo(SOME_FIRST_NAME)
                .jsonPath("$.last").isEqualTo(SOME_LAST_NAME)
                .consumeWith(document("get",
                        ID_PATH_PARAMETERS_DESCRIPTOR,
                        responseFields(PERSON_DESCRIPTOR)
                ));

        var nonExistingId = 12345678L;
        assertThat(nonExistingId).isNotEqualTo(id);

        // Ensure GET returns not found for an unknown ID
        webTestClient.get()
                .uri("/person/{id}", nonExistingId)
                .exchange()
                .expectStatus().isNotFound();

        // List
        webTestClient.get()
                .uri("/person")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(id)
                .jsonPath("$[0].first").isEqualTo(SOME_FIRST_NAME)
                .jsonPath("$[0].last").isEqualTo(SOME_LAST_NAME)
                .consumeWith(document("list",
                        responseFields(
                                fieldWithPath("[]").description("All stored person entries")
                        ).andWithPrefix("[].", PERSON_DESCRIPTOR)
                ));

        // Update
        webTestClient.put()
                .uri("/person/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "first": "%s",
                          "last": "%s"
                        }
                        """.formatted(OTHER_FIRST_NAME, OTHER_LAST_NAME))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.first").isEqualTo(OTHER_FIRST_NAME)
                .jsonPath("$.last").isEqualTo(OTHER_LAST_NAME)
                .consumeWith(document("update",
                        ID_PATH_PARAMETERS_DESCRIPTOR,
                        requestFields(
                                fieldWithPath("first").description("New first name of the person"),
                                fieldWithPath("last").description("New last name of the person")
                        ),
                        responseFields(PERSON_DESCRIPTOR)
                ));

        // Ensure PUT returns NOT FOUND when attempting to update a non-existing person
        webTestClient.put()
                .uri("/person/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "first": "%s",
                          "last": "%s"
                        }
                        """.formatted(SOME_FIRST_NAME, SOME_LAST_NAME))
                .exchange()
                .expectStatus().isNotFound();

        // Delete
        webTestClient.delete()
                .uri("/person/{id}", id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(document("delete"));

        // Ensure it's not found anymore
        webTestClient.get()
                .uri("/person/{id}", id)
                .exchange()
                .expectStatus().isNotFound();

        webTestClient.get()
                .uri("/person")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);

        // Ensure deletion is idempotent
        webTestClient.delete()
                .uri("/person/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }
}
