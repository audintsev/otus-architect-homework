package me.udintsev.otus.architect.homework6.person;

import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
public class PersonService {
    private final DatabaseClient databaseClient;

    private static final String TABLE_NAME = "person";
    private static final Function<Row, Person> USER_MAPPER = row -> new Person(
            row.get("id", Long.class),
            row.get("first", String.class),
            row.get("last", String.class)
    );

    public PersonService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<Person> list() {
        return databaseClient.sql("SELECT id, first, last from %s".formatted(TABLE_NAME))
                .map(USER_MAPPER)
                .all();
    }

    public Mono<Person> create(String first, String last) {
        return databaseClient.sql("INSERT INTO %s (first, last) VALUES (:first, :last)".formatted(TABLE_NAME))
                .filter(statement -> statement.returnGeneratedValues("id"))
                .bind("first", first)
                .bind("last", last)
                .map(row -> new Person(row.get("id", Long.class), first, last))
                .first();
    }
}
