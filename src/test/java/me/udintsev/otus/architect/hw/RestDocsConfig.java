package me.udintsev.otus.architect.hw;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsWebTestClientConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentationConfigurer;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@TestConfiguration(proxyBeanMethods = false)
public class RestDocsConfig implements RestDocsWebTestClientConfigurationCustomizer {
    @Override
    public void customize(WebTestClientRestDocumentationConfigurer configurer) {
        configurer.snippets()
                .withEncoding("UTF-8")
                .withDefaults(HttpDocumentation.httpRequest(), HttpDocumentation.httpResponse());
        configurer.operationPreprocessors()
                .withRequestDefaults(
                        removeHeaders("Host"),
                        modifyUris()
                                .scheme("http")
                                .host("arch.homework")
                                .removePort()
                )
                .withResponseDefaults(
                        prettyPrint(),
                        removeHeaders("Content-Length", "Expires", "Pragma", "Cache-Control",
                                "X-XSS-Protection", "X-Content-Type-Options", "X-Frame-Options")
                );
    }
}
