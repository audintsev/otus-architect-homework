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
    private static final Function<Row, Person> MAPPER = row -> new Person(
            row.get("id", Long.class),
            row.get("first", String.class),
            row.get("last", String.class)
    );

    private static final String SELECT_BASE = "SELECT id, first, last from %s".formatted(TABLE_NAME);

    public PersonService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Person> get(long id) {
        return databaseClient.sql("%s where id=:id".formatted(SELECT_BASE))
                .bind("id", id)
                .map(MAPPER)
                .one();
    }

    public Flux<Person> list() {
        return databaseClient.sql(SELECT_BASE)
                .map(MAPPER)
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

    public Mono<Person> update(long id, String first, String last) {
        return databaseClient.sql("UPDATE %s SET first=:first, last=:last WHERE id=:id".formatted(TABLE_NAME))
                .bind("id", id)
                .bind("first", first)
                .bind("last", last)
                .fetch().rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0
                        ? Mono.just(new Person(id, first, last))
                        : Mono.empty()
                );
    }

    public Mono<Void> delete(long id) {
        return databaseClient.sql("DELETE FROM %s WHERE id=:id".formatted(TABLE_NAME))
                .bind("id", id)
                .then();
    }
}
