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
    public static final String URI_BASE = "/person";
    public static final String URI_WITH_ID = "%s/{%s}".formatted(PersonHandler.URI_BASE, PersonHandler.PATH_VARIABLE_ID);
    public static final String PATH_VARIABLE_ID = "id";

    private final PersonService personService;

    public PersonHandler(PersonService personService) {
        this.personService = personService;
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return personMonoToServerResponse(personService.get(idFromRequest(request)));
    }

    public Mono<ServerResponse> list() {
        var list = personService.list();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(list, Person.class);
    }

    @Data
    static class PersonUpdateRequest {
        String first;
        String last;
    }

    @Transactional
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(PersonUpdateRequest.class)
                .flatMap(person -> personService.create(person.getFirst(), person.getLast()))
                .flatMap(person ->
                        ServerResponse
                                .created(URI.create(String.format("%s/%s", URI_BASE, person.getId())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(person));
    }

    @Transactional
    public Mono<ServerResponse> update(ServerRequest request) {
        var personMono = request.bodyToMono(PersonUpdateRequest.class)
                .flatMap(person -> personService.update(idFromRequest(request), person.getFirst(), person.getLast()));
        return personMonoToServerResponse(personMono);
    }

    @Transactional
    public Mono<ServerResponse> delete(ServerRequest request) {
        return personService.delete(idFromRequest(request))
                .then(ServerResponse.noContent().build());
    }

    private static long idFromRequest(ServerRequest request) {
        return Long.parseLong(request.pathVariable("id"));
    }

    private Mono<ServerResponse> personMonoToServerResponse(Mono<Person> personMono) {
        return personMono.hasElement()
                .flatMap(hasElement ->
                        hasElement ? ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(personMono, Person.class)
                                : ServerResponse.notFound().build());
    }
}
