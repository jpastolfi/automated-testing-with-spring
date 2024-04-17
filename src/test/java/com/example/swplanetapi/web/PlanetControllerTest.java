package com.example.swplanetapi.web;

import com.example.swplanetapi.domain.Planet;
import com.example.swplanetapi.domain.PlanetService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.swplanetapi.common.PlanetConstants.EMPTYPLANET;
import static com.example.swplanetapi.common.PlanetConstants.NULLPLANET;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static com.example.swplanetapi.common.PlanetConstants.PLANET;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(PlanetController.class)
public class PlanetControllerTest {

  @MockBean
  private PlanetService planetService;

  // @Autowired
  // private TestEntityManager testEntityManager;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;
  @Test
  public void createPlanet_WithValidData_ReturnsCreated() throws Exception {
    // Mockamos o retorno da service, que será chamada pela controller
    when(planetService.create(PLANET)).thenReturn(PLANET);
    // Cliente Http para fazer requisições
    mockMvc
        .perform(
        // Método post do request builder com a rota
            post("/planets")
                // Conteúdo do corpo da requisição. Aqui passamos o objeto PLANET, convertido para
                // String pelo objectMapper
                .content(objectMapper.writeValueAsString(PLANET))
                // Deixamos explícito que o tipo de conteúdo é JSON
                .contentType(MediaType.APPLICATION_JSON))
        // Usamos o ResultMatchers para usar o método status para verificar se o status da resposta
        // é 201
        .andExpect(status().isCreated())
        // Usamos o ResultMatchers para usar o método jsonPath para verificar se o corpo da resposta
        // é igual ao objeto passado no corpo da requisição. Se quisessemos verificar as chaves do
        // objeto, poderíamos usar .andExpect(jsonPath("$.chaveDaResposta").value(valorEsperado))
        .andExpect(jsonPath("$").value(PLANET));
  }

  @Test
  public void createPlanet_WithInvalidData_ReturnsBadRequest() throws Exception {
    mockMvc.perform(post("/planets")
            .content(objectMapper.writeValueAsString(EMPTYPLANET))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity());

    mockMvc.perform(post("/planets")
            .content(objectMapper.writeValueAsString(NULLPLANET))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void getPlanet_ByExistingId_ReturnsPlanet() throws Exception {
    when(planetService.getById(any())).thenReturn(Optional.of(PLANET));
    mockMvc.perform(get(
        "/planets/1"
    ).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(PLANET));
  }

  @Test
  public void getPlanet_ByNonExistingId_ThrowsException() throws Exception {
    when(planetService.getById(any())).thenReturn(Optional.empty());
    mockMvc.perform(get("/planets/1")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
