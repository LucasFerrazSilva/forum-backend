package com.ferraz.forumbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ferraz.forumbackend.integration.fixture.SessionFixture;
import com.ferraz.forumbackend.integration.fixture.UserFixture;
import com.ferraz.forumbackend.integration.util.HttpMethod;
import com.ferraz.forumbackend.integration.util.MvcRequestBuilder;
import com.ferraz.forumbackend.integration.util.TestcontainersConfig;
import com.ferraz.forumbackend.integration.util.TestcontainersInitializer;
import com.icegreen.greenmail.util.GreenMail;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration(initializers = TestcontainersInitializer.class)
public abstract class AbstractIntegrationTest {

    protected final GreenMail greenMail = TestcontainersConfig.GREEN_MAIL;

    @Autowired
    private Flyway flyway;

    @Autowired
    private MockMvc mvc;

    @Autowired
    protected UserFixture userFixture;

    @Autowired
    protected SessionFixture sessionFixture;

    @Value("${server.cookie.name}")
    protected String cookieName;

    protected ObjectMapper objectMapper;

    public AbstractIntegrationTest() {
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeAll
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @BeforeEach
    void resetEmails() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    public abstract String getEndpoint();

    public MvcRequestBuilder GET() {
        return createMvcRequestBuilder(HttpMethod.GET);
    }

    public MvcRequestBuilder DELETE() {
        return createMvcRequestBuilder(HttpMethod.DELETE);
    }

    public MvcRequestBuilder POST() {
        return createMvcRequestBuilder(HttpMethod.POST);
    }

    public MvcRequestBuilder PATCH() {
        return createMvcRequestBuilder(HttpMethod.PATCH);
    }

    private MvcRequestBuilder createMvcRequestBuilder(HttpMethod method) {
        return new MvcRequestBuilder(mvc, sessionFixture, objectMapper, method, getEndpoint());
    }

    public <T> T extractObject(MockHttpServletResponse response, Class<T> clazz) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), clazz);
    }

}
