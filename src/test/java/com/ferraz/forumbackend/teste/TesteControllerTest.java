package com.ferraz.forumbackend.teste;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TesteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TesteRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Teste")
    void teste() throws Exception {
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/teste")).andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        JsonNode jsonNode = objectMapper.readTree(response.getContentAsString());
        String responseValue = jsonNode.get("valor").asText();
        assertThat(responseValue).isNotBlank();

        Optional<TesteEntity> teste = repository.findById(1);
        assertThat(teste).isPresent();
        assertThat(teste.get().getValor().toString()).isEqualTo(responseValue);
    }

}