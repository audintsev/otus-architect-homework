package me.udintsev.otus.architect.homework6;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

public class PersonHandler {
    private final URI baseUri;
    private final PersonService personService;

    public PersonHandler(URI baseUri, PersonService personService) {
        this.baseUri = baseUri;
        this.personService = personService;
    }

    public RouterFunction<?> routerFunction() {
        return RouterFunctions.route()
                .GET(baseUri.toString(), this::list)
                .POST(baseUri.toString(), this::insert)
                .build();
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
                                .created(baseUri.resolve(String.valueOf(person.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(person));
    }
}
