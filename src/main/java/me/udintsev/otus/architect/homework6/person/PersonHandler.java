package me.udintsev.otus.architect.homework6.person;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class PersonHandler {
    public static final String BASE_PATH = "/person";
    private static final URI BASE_URI = URI.create(BASE_PATH);

    private final PersonService personService;

    public PersonHandler(PersonService personService) {
        this.personService = personService;
    }

    public Mono<ServerResponse> list(ServerRequest request) {
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

    public Mono<ServerResponse> insert(ServerRequest request) {
        return request.bodyToMono(CreatePersonRequest.class)
                .flatMap(person -> personService.insert(person.getFirst(), person.getLast()))
                .flatMap(person ->
                        ServerResponse
                                .created(BASE_URI.resolve(String.valueOf(person.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(person));
    }
}
