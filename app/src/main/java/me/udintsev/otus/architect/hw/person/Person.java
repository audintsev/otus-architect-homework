package me.udintsev.otus.architect.hw.person;

import lombok.Value;

@Value
public class Person {
    Long id;
    String email;
    String firstName;
    String lastName;
}
