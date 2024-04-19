package com.example.swplanetapi;

import static com.example.swplanetapi.common.PlanetConstants.ALDERAAN;
import static com.example.swplanetapi.common.PlanetConstants.PLANET;

import com.example.swplanetapi.domain.Planet;

import static com.example.swplanetapi.common.PlanetConstants.TATOOINE;
import static com.example.swplanetapi.common.PlanetConstants.YAVINIV;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = { "/remove_planets.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = { "/import_planets.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class PlanetIT {
  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private WebTestClient webTestClient;

  @Test
  public void createPlanet_ReturnsCreated() {
    Planet sut = webTestClient.post().uri("/planets").bodyValue(TATOOINE)
        .exchange().expectStatus().isCreated().expectBody(Planet.class)
        .returnResult().getResponseBody();

    assertThat(sut.getName()).isEqualTo(TATOOINE.getName());
    assertThat(sut.getClimate()).isEqualTo(TATOOINE.getClimate());
    assertThat(sut.getTerrain()).isEqualTo(TATOOINE.getTerrain());
    /*ResponseEntity<Planet> sut = testRestTemplate.postForEntity("/planets", PLANET, Planet.class);
    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(sut.getBody()).isInstanceOf(Planet.class);
    assertThat(sut.getBody().getId()).isNotNull();
    assertThat(sut.getBody().getName()).isEqualTo(PLANET.getName());
    assertThat(sut.getBody().getClimate()).isEqualTo(PLANET.getClimate());
    assertThat(sut.getBody().getTerrain()).isEqualTo(PLANET.getTerrain());*/
  }

  @Test
  public void getPlanet_ReturnsPlanet() {
    ResponseEntity<Planet> sut = testRestTemplate.getForEntity("/planets/1", Planet.class);
    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sut.getBody()).isEqualTo(TATOOINE);
  }

  @Test
  public void getPlanetByName_ReturnsPlanet() {
    ResponseEntity<Planet> sut = testRestTemplate.getForEntity("/planets/name/%s".formatted(TATOOINE.getName()), Planet.class);
    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sut.getBody()).isEqualTo(TATOOINE);
  }

  @Test
  public void listPlanets_ReturnsAllPlanets() {
    ResponseEntity<Planet[]> sut = testRestTemplate.getForEntity("/planets", Planet[].class);
    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sut.getBody()).hasSize(3);
    assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
    assertThat(sut.getBody()[1]).isEqualTo(ALDERAAN);
    assertThat(sut.getBody()[2]).isEqualTo(YAVINIV);
  }

  @Test
  public void listPlanets_ByClimate_ReturnsPlanets() {
    ResponseEntity<Planet[]> sut = testRestTemplate.getForEntity("/planets?climate=%s".formatted(TATOOINE.getClimate()),
        Planet[].class);

    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sut.getBody()).hasSize(1);
    assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
  }

  @Test
  public void listPlanets_ByTerrain_ReturnsPlanets() {
    ResponseEntity<Planet[]> sut = testRestTemplate.getForEntity("/planets?terrain=%s".formatted(TATOOINE.getTerrain()),
        Planet[].class);

    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sut.getBody()).hasSize(1);
    assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
  }

  @Test
  public void listPlanets_ByClimateAndTerrain_ReturnsPlanets() {
    ResponseEntity<Planet[]> sut = testRestTemplate.getForEntity("/planets?climate=%s&terrain=%s".formatted(TATOOINE.getClimate(), TATOOINE.getTerrain()),
        Planet[].class);

    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(sut.getBody()).hasSize(1);
    assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
  }

  @Test
  public void removePlanet_ReturnsNoContent() {
    ResponseEntity<Void> sut = testRestTemplate.exchange("/planets/1", HttpMethod.DELETE, null, Void.class);
    assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(sut.getBody()).isNull();
  }
}
