package me.udintsev.otus.architect.hw;

import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.PostgreSQLR2DBCDatabaseContainer;

@TestConfiguration
public class DatabaseConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLR2DBCDatabaseContainer jdbcDatabaseContainer() {
        return new PostgreSQLR2DBCDatabaseContainer(
                new PostgreSQLContainer<>().withInitScript("schema.sql")
        );
    }

    @Bean
    public ConnectionFactory connectionFactory(PostgreSQLR2DBCDatabaseContainer jdbcDatabaseContainer) {
        ConnectionFactoryOptions options = jdbcDatabaseContainer.configure(ConnectionFactoryOptions.builder().build());
        var connectionFactoryProvider = new PostgresqlConnectionFactoryProvider();
        return connectionFactoryProvider.create(options);
    }
}

