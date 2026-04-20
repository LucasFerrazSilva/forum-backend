package com.ferraz.forumbackend.integration.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ferraz.forumbackend.integration.fixture.SessionFixture;
import com.ferraz.forumbackend.session.dto.LoginDTO;
import jakarta.servlet.http.Cookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class MvcRequestBuilder {

    private final MockMvc mvc;
    private final SessionFixture sessionFixture;
    private final ObjectMapper objectMapper;

    private final HttpMethod method;
    private String endpoint;
    private String requestBody = "";
    private boolean authenticateWithNewUser = false;
    private LoginDTO loginDTO = null;
    private Cookie sessionCookie = null;


    public MvcRequestBuilder(MockMvc mvc, SessionFixture sessionFixture, ObjectMapper objectMapper, HttpMethod method, String endpoint) {
        this.mvc = mvc;
        this.sessionFixture = sessionFixture;
        this.objectMapper = objectMapper;

        this.method = method;
        this.endpoint = endpoint;
    }


    public MvcRequestBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public MvcRequestBuilder withRequestBody(Object requestBody) throws JsonProcessingException {
        this.requestBody = objectMapper.writeValueAsString(requestBody);
        return this;
    }

    public MvcRequestBuilder shouldAuthenticateWithNewUser(boolean authenticateWithNewUser) {
        this.authenticateWithNewUser = authenticateWithNewUser;
        return this;
    }

    public MvcRequestBuilder withLoginDTO(LoginDTO loginDTO) {
        this.loginDTO = loginDTO;
        return this;
    }

    public MvcRequestBuilder withSessionCookie(Cookie sessionCookie) {
        this.sessionCookie = sessionCookie;
        return this;
    }

    public MockHttpServletResponse send() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = method.getMockMvcRequestBuilder(endpoint, requestBody);

        if (authenticateWithNewUser || sessionCookie != null || loginDTO != null) {
            if (sessionCookie == null) {
                sessionCookie = sessionFixture.cookie(loginDTO);
            }

            requestBuilder.cookie(sessionCookie);
        }

        return  mvc.perform(requestBuilder).andReturn().getResponse();
    }


}
