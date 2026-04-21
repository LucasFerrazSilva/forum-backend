package com.ferraz.forumbackend.integration.util;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class TestcontainersInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                "spring.mail.host=localhost",
                "spring.mail.port=" + TestcontainersConfig.GREEN_MAIL.getSmtp().getPort()
        ).applyTo(context.getEnvironment());
    }
}

