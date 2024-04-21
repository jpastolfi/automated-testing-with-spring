package com.example.swplanetapi.web;

import com.example.swplanetapi.domain.Planet;
import com.example.swplanetapi.domain.PlanetService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.swplanetapi.common.PlanetConstants.EMPTYPLANET;
import static com.example.swplanetapi.common.PlanetConstants.NULLPLANET;
import static com.example.swplanetapi.common.PlanetConstants.PLANETS;
import static com.example.swplanetapi.common.PlanetConstants.TATOOINE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    when(planetService.getById(anyLong())).thenReturn(Optional.of(PLANET));
    mockMvc.perform(get(
        "/planets/1"
    ).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(PLANET));
  }

  @Test
  public void getPlanet_ByNonExistingId_ThrowsException() throws Exception {
    when(planetService.getById(anyLong())).thenReturn(Optional.empty());

    mockMvc.perform(get("/planets/1")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void getPlanet_ByExistingName_ReturnsPlanet() throws Exception {
    when(planetService.getByName(anyString())).thenReturn(Optional.of(PLANET));
    mockMvc.perform(get(
        "/planets/name/anything"
    )
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(PLANET));
  }

  @Test
  public void getPlanet_ByNonExistingName_ThrowsException() throws Exception {
    when(planetService.getByName(anyString())).thenReturn(Optional.empty());
    mockMvc.perform(get(
        "/planets/name/anything"
        )
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void listPlanets_ReturnsFilteredPlanets() throws Exception {
    when(planetService.list(null, null)).thenReturn(PLANETS);
    when(planetService.list(TATOOINE.getClimate(), TATOOINE.getTerrain())).thenReturn(List.of(TATOOINE));

    mockMvc.perform(get(
        "/planets"
    ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)));

    mockMvc.perform(get(
        "/planets?climate=%s&terrain=%s".formatted(TATOOINE.getClimate(), TATOOINE.getTerrain())
    ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0]").value(TATOOINE));
  }

  @Test
  public void listPlanets_ReturnsNoPlanets() throws Exception {
    when(planetService.list(anyString(), anyString())).thenReturn(List.of());
    mockMvc.perform(get(
            "/planets?terrain=%s&climate=%s".formatted(TATOOINE.getClimate(), TATOOINE.getTerrain())
        ))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void removePlanet_WithExistingId_ReturnsNoContent() throws Exception {
    mockMvc.perform(delete(
        "/planets/1"
    ))
        .andExpect(status().isNoContent());
  }

  @Test
  public void removePlanet_WithNonExistingId_ReturnsNotFound() throws Exception {
    doThrow(new EmptyResultDataAccessException(1)).when(planetService).deleteById(anyLong());
    mockMvc.perform(delete(
        "/planets/1"
    ))
        .andExpect(status().isNotFound());
  }
}
