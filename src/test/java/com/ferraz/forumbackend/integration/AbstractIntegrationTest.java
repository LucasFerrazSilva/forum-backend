package com.ferraz.forumbackend.integration;

import com.ferraz.forumbackend.integration.util.TestcontainersConfig;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @BeforeAll
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }
}

