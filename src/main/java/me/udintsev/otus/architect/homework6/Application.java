package me.udintsev.otus.architect.homework6;

import me.udintsev.otus.architect.homework6.person.PersonHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@SpringBootApplication(proxyBeanMethods = false)
@EnableWebFlux
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RouterFunction<?> routerFunction(PersonHandler personHandler) {
        return RouterFunctions.route()
                .GET(PersonHandler.URI_WITH_ID, personHandler::get)
                .GET(PersonHandler.URI_BASE, req -> personHandler.list())
                .POST(PersonHandler.URI_BASE, personHandler::create)
                .PUT(PersonHandler.URI_WITH_ID, personHandler::update)
                .DELETE(PersonHandler.URI_WITH_ID, personHandler::delete)
                .resources("/**", new ClassPathResource("static/"))
                .build();
    }
}
