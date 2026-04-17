package com.ferraz.forumbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ferraz.forumbackend.integration.fixture.UserFixture;
import com.ferraz.forumbackend.integration.session.SessionControllerIntegrationTest;
import com.ferraz.forumbackend.integration.util.TestcontainersConfig;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import com.ferraz.forumbackend.user.UserEntity;
import jakarta.servlet.http.Cookie;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private MockMvc mvc;

    @Value("${server.cookie.name}")
    private String cookieName;


    protected ObjectMapper objectMapper;

    @Autowired
    protected UserFixture userFixture;


    @BeforeAll
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // GET
    public MockHttpServletResponse get(String endpoint) throws Exception {
        return get(endpoint, false);
    }

    public MockHttpServletResponse get(String endpoint, boolean login) throws Exception {
        return get(endpoint, login, null);
    }

    public MockHttpServletResponse get(String endpoint, boolean login, LoginDTO loginDTO) throws Exception {
        Cookie sessionCookie = null;

        if (login) {
            if (loginDTO == null) {
                String senhaValida = "SenhaValida";
                UserEntity user = userFixture.user(b -> b.password(senhaValida));
                loginDTO = new LoginDTO(user.getEmail(), senhaValida);
            }
            String requestBody = objectMapper.writeValueAsString(loginDTO);
            MockHttpServletResponse response = post(SessionControllerIntegrationTest.ENDPOINT, requestBody);
            List<Cookie> cookies = List.of(response.getCookies());
            sessionCookie = cookies.getFirst();
        }

        return get(endpoint, sessionCookie);
    }

    public MockHttpServletResponse get(String endpoint, Cookie sessionCookie) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(endpoint);
        return perform(requestBuilder, sessionCookie);
    }

    // DELETE
    public MockHttpServletResponse delete(String endpoint, Cookie sessionCookie) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete(endpoint);
        return perform(requestBuilder, sessionCookie);
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder requestBuilder, Cookie sessionCookie) throws Exception {
        if (sessionCookie != null) {
            requestBuilder.cookie(sessionCookie);
        }

        return  mvc.perform(requestBuilder).andReturn().getResponse();
    }

    public MockHttpServletResponse post(String endpoint, String requestBody) throws Exception {
        return  mvc.perform(
                MockMvcRequestBuilders.post(endpoint)
                        .contentType("application/json")
                        .content(requestBody)
        ).andReturn().getResponse();
    }

    public MockHttpServletResponse patch(String endpoint, String requestBody) throws Exception {
        return  mvc.perform(
                MockMvcRequestBuilders.patch(endpoint)
                        .contentType("application/json")
                        .content(requestBody)
        ).andReturn().getResponse();
    }

}

