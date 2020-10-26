package me.udintsev.otus.architect.homework6;

import me.udintsev.otus.architect.homework6.person.PersonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@Configuration(proxyBeanMethods = false)
public class RouterConfig {
    @Bean
    public RouterFunction<?> personRouterFn(PersonHandler personHandler) {
        return RouterFunctions.route()
                .GET(PersonHandler.BASE_PATH, req -> personHandler.list())
                .POST(PersonHandler.BASE_PATH, personHandler::create)
                .build();
    }
}
