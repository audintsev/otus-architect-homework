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
                .GET(PersonHandler.URI_WITH_ID, personHandler::get)
                .GET(PersonHandler.URI_BASE, req -> personHandler.list())
                .POST(PersonHandler.URI_BASE, personHandler::create)
                .PUT(PersonHandler.URI_WITH_ID, personHandler::update)
                .DELETE(PersonHandler.URI_WITH_ID, personHandler::delete)
                .build();
    }
}
