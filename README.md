# Homework for the lesson #6 (Kubernetes basics) of the [Otus "Software Architect" course](https://otus.ru/lessons/arhitektor-po/) 

## Kubernetes

TODO

## Application

Application-wise, this repository showcases the following technologies being used in one simple web application:

* Reactive stack: Spring WebFlux and R2DBC
* Testcontainers to run integration tests with Postgresql
* Spring REST Docs to generate API documentation out of API tests

TODO (?):

* Building native image
* CI with Github actions (possibly publishing generated API docs as a Github pages site)

Comments:

* I don't use embedded DB migrations (managed by Liquibase or Flyway). Tests create schema defined in
[schema.sql](src/test/resources/schema.sql). When running _productively_ schema needs to be pre-created
by a Kubernetes job. The reasons for choosing this design are:
  * I'd like to experiment with Kubernetes jobs
  * neither Liquibase nor Flyway can natively work R2DBC connections
  * most importantly, my current impression is that app-managed DB migrations feel alien in the new
microservices/cloud native world: with app-managed migrations an application typically refuses to run if actual schema
version doesn't match what application expects; but that's exactly what seems to happen more often than not:
a new application version makes some additions to the schema (e.g. a new column), and the two versions - the old
one and the new one - co-exist in the same deployment. 
* A lot of R2DBC foundation stuff is being moved from Spring Data R2DBC to Spring Framework 5.3. I decided to go ahead
and use the new stuff from Soring 5.3. But at the time of this writing Spring Framework 5.3 is still only available as
a milestone release (as a dependency of a milestone release of Spring Boot 2.4). That's how I ended up using pre-release
versions of Spring
* Even though API documentation _is_ generated, Boot difficulties serving it (as embedded static content), perhaps
due to the pre-release Boot version being used

### App: building and running

Building:

```
./gradlew bootJar
```

Running (assuming Postgres is available on localhost:5432):

```
java -jar otus-architect-homework6-0.0.1-SNAPSHOT.jar --spring.r2dbc.url=r2dbc:postgresql://localhost/test --spring.r2dbc.username=test --spring.r2dbc.password=test
```

### Image: building and running

Building:

```
./gradlew bootBuildImage --imageName=udintsev/otus-architect-homework6:0.1
```

Running (assuming Postgres is available on localhost:5432, which is not):

```
docker run -e spring.r2dbc.url=r2dbc:postgresql://localhost/test -e spring.r2dbc.username=test -e spring.r2dbc.password=test -p 8080:8080 udintsev/otus-architect-homework6:0.1
```

### Useful links

* [R2DBC on Spring Framework](https://docs.spring.io/spring-framework/docs/5.3.0-RC2/reference/html/data-access.html#r2dbc)
* [WebFllux on Spring Framework](https://docs.spring.io/spring-framework/docs/5.3.0-RC2/reference/html/web-reactive.html#spring-webflux)
* [R2DBC support in Testcontainers](https://www.testcontainers.org/modules/databases/r2dbc/)
* [Spring REST Docs](https://docs.spring.io/spring-restdocs/docs/current/reference/html5/) 
