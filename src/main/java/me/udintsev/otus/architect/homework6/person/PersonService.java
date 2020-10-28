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
    private static final String COL_ID = "id";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";

    private static final Function<Row, Person> MAPPER = row -> new Person(
            row.get(COL_ID, Long.class),
            row.get(COL_FIRST_NAME, String.class),
            row.get(COL_LAST_NAME, String.class)
    );

    private static final String SELECT_BASE = String.format(
            "SELECT %s, %s, %s from %s",
            COL_ID, COL_FIRST_NAME, COL_LAST_NAME, TABLE_NAME);

    public PersonService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Person> get(long id) {
        return databaseClient.sql(String.format("%s where %s=:id", SELECT_BASE, COL_ID))
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
        return databaseClient.sql(
                String.format(
                        "INSERT INTO %s (%s, %s) VALUES (:first, :last)",
                        TABLE_NAME, COL_FIRST_NAME, COL_LAST_NAME))
                .filter(statement -> statement.returnGeneratedValues("id"))
                .bind("first", first)
                .bind("last", last)
                .map(row -> new Person(row.get("id", Long.class), first, last))
                .first();
    }

    public Mono<Person> update(long id, String first, String last) {
        return databaseClient.sql(
                String.format(
                        "UPDATE %s SET %s=:first, %s=:last WHERE %s=:id",
                        TABLE_NAME, COL_FIRST_NAME, COL_LAST_NAME, COL_ID))
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
        return databaseClient.sql(
                String.format(
                        "DELETE FROM %s WHERE %s=:id",
                        TABLE_NAME, COL_ID))
                .bind("id", id)
                .then();
    }
}
