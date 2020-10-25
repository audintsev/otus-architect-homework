package me.udintsev.otus.architect.homework6;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DatabaseConfig.class)
class PersonServiceTest {
    @Autowired
    PersonService personService;

    @Test
    void insertAndListUsers() {
        var user1 = personService.insertUser("SomeFirst", "SomeLast").block();
        assertThat(user1).isNotNull();
        assertThat(user1.getId()).isNotNull();
        assertThat(user1.getFirst()).isEqualTo("SomeFirst");
        assertThat(user1.getLast()).isEqualTo("SomeLast");

        var user2 = personService.insertUser("AnotherFirst", "AnotherLast").block();
        assertThat(user2).isNotNull();
        assertThat(user2.getId()).isNotNull();
        assertThat(user2.getFirst()).isEqualTo("AnotherFirst");
        assertThat(user2.getLast()).isEqualTo("AnotherLast");

        StepVerifier.create(personService.listUsers())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();
    }
}
