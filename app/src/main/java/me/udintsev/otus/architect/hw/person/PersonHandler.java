package me.udintsev.otus.architect.hw.person;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@Transactional(readOnly = true)
public class PersonHandler {
    public static final String URI_BASE = "/person";
    public static final String URI_REGISTER = "%s/register".formatted(URI_BASE);
    public static final String PATH_VARIABLE_ID = "id";
    public static final String URI_WITH_ID = "%s/{%s}".formatted(URI_BASE, PATH_VARIABLE_ID);

    private final Validator validator;
    private final PersonService personService;

    public PersonHandler(Validator validator,
                         PersonService personService) {
        this.validator = validator;
        this.personService = personService;
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return personService.get(idFromRequest(request))
                .flatMap(person -> ServerResponse.ok().bodyValue(person))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> list() {
        var list = personService.list();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(list, Person.class);
    }

    @Data
    @Valid
    static class PersonCreateRequest {
        @NotNull
        String email;

        @NotNull
        String firstName;

        @NotNull
        String lastName;
    }

    @Data
    @Valid
    static class PersonUpdateRequest {
        @NotNull
        String firstName;

        @NotNull
        String lastName;
    }

    @Transactional
    public Mono<ServerResponse> register() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication.getCredentials() instanceof AbstractOAuth2Token)
                .map(Authentication::getCredentials)
                .cast(Jwt.class)
                .flatMap(jwt -> {
                    var claims = jwt.getClaims();
                    String email = (String)claims.get("email");
                    String firstName = (String)claims.get("given_name");
                    String lastName = (String)claims.get("family_name");
                    return personService.create(email, firstName, lastName)
                            .flatMap(person -> ServerResponse
                                    .created(URI.create(String.format("%s/%s", URI_BASE, person.getId())))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(person));
                });
    }

    @Transactional
    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(PersonCreateRequest.class)
                .flatMap(body -> validateAndThen(body, () ->
                        personService.create(body.getEmail(), body.getFirstName(), body.getLastName())
                                .flatMap(person -> ServerResponse
                                        .created(URI.create(String.format("%s/%s", URI_BASE, person.getId())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(person))
                ));
    }

    @Transactional
    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(PersonUpdateRequest.class)
                .flatMap(body -> validateAndThen(body, () -> {
                        long id = idFromRequest(request);
                        return personService.get(id)
                                .flatMap(person ->
                                    personService.update(person.getId(), person.getEmail(), body.getFirstName(), body.getLastName())
                                )
                                .flatMap(person -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(person))
                                .switchIfEmpty(ServerResponse.notFound().build());
                }));
    }

    @Transactional
    public Mono<ServerResponse> delete(ServerRequest request) {
        return personService.delete(idFromRequest(request))
                .then(ServerResponse.noContent().build());
    }

    private static long idFromRequest(ServerRequest request) {
        return Long.parseLong(request.pathVariable("id"));
    }

    private <T> Mono<ServerResponse> validateAndThen(T body, Supplier<Mono<ServerResponse>> thenHandler) {
        var errors = new BeanPropertyBindingResult(body, body.getClass().getName());
        validator.validate(body, errors);
        if (errors.getAllErrors().isEmpty()) {
            return thenHandler.get();
        } else {
            Map<String, String> map = new HashMap<>();
            errors.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                map.put(fieldName, errorMessage);
            });

            return ServerResponse.badRequest().bodyValue(map);
        }
    }
}
