package com.ferraz.forumbackend.integration.util;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withReuse(true);

    public static final GreenMail GREEN_MAIL =
            new GreenMail(new ServerSetup(0, "localhost", ServerSetup.PROTOCOL_SMTP))
                    .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    static {
        POSTGRES.start();
        GREEN_MAIL.start();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES;
    }

    @Bean
    GreenMail greenMail() {
        return GREEN_MAIL;
    }

}
