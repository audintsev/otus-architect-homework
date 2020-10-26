package me.udintsev.otus.architect.homework6.person;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Transactional(readOnly = true)
public class PersonHandler {
    public static final String BASE_PATH = "/person";

    private final PersonService personService;

    public PersonHandler(PersonService personService) {
        this.personService = personService;
    }

    public Mono<ServerResponse> list() {
        var list = personService.list();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(list, Person.class);
    }

    @Data
    static class CreatePersonRequest {
        String first;
        String last;
    }

    @Transactional
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreatePersonRequest.class)
                .flatMap(person -> personService.create(person.getFirst(), person.getLast()))
                .flatMap(person ->
                        ServerResponse
                                .created(URI.create(String.format("%s/%s", BASE_PATH, person.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(person));
    }
}
