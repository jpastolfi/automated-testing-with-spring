package com.example.swplanetapi.domain;

import static com.example.swplanetapi.common.PlanetConstants.EMPTYPLANET;
import static com.example.swplanetapi.common.PlanetConstants.NULLPLANET;
import static com.example.swplanetapi.common.PlanetConstants.PLANET;
import static com.example.swplanetapi.common.PlanetConstants.TATOOINE;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.test.context.jdbc.Sql;

// Notação para criação de um banco H2 para os testes
@DataJpaTest
public class PlanetRepositoryTest {

  @Autowired
  private PlanetRepository planetRepository;

  // Permite interagir com o banco de dados sem usar a repository
  @Autowired
  private TestEntityManager testEntityManager;

  @AfterEach
  private void afterEach() {
    PLANET.setId(null);
  }

  @Test
  public void createPlanet_WithValidData_ReturnsPlanet() {
    Planet planet = planetRepository.save(PLANET);
    Planet sut = testEntityManager.find(Planet.class, planet.getId());
    assertThat(sut).isNotNull();
    assertThat(sut.getName()).isEqualTo(planet.getName());
    assertThat(sut.getClimate()).isEqualTo(planet.getClimate());
    assertThat(sut.getTerrain()).isEqualTo(planet.getTerrain());
  }

  @ParameterizedTest
  @MethodSource("providesInvalidPlanets")
  public void createPlanet_WithInvalidData_ThrowsException(Planet planet) {
    assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
  }

  private static Stream<Arguments> providesInvalidPlanets() {
    return Stream.of(
        Arguments.of(new Planet(null, "climate", "terrain")),
        Arguments.of(new Planet("name", null, "terrain")),
        Arguments.of(new Planet("name", "climate", null)),
        Arguments.of(new Planet(null, null, "terrain")),
        Arguments.of(new Planet(null, "climate", null)),
        Arguments.of(new Planet("name", null, null)),
        Arguments.of(new Planet(null, null, null)),
        Arguments.of(new Planet("", "climate", "terrain")),
        Arguments.of(new Planet("name", "", "terrain")),
        Arguments.of(new Planet("name", "climate", "")),
        Arguments.of(new Planet("", "", "terrain")),
        Arguments.of(new Planet("", "climate", "")),
        Arguments.of(new Planet("name", "", "")),
        Arguments.of(new Planet("", "", ""))
    );
  }

  @Test
  public void createPlanet_WithExistingName_ThrowsException() {
    // Salva no banco sem usar a repository. O método persist salva o planeta, mas precisamos fazer
    // o flush para atualizar o banco de dados e posteriormente o find para buscar a entidade
    // que foi salva.
    Planet planet = testEntityManager.persistFlushFind(PLANET);
    testEntityManager.detach(planet);
    planet.setId(null);
    assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
  }

  @Test
  public void getPlanet_ByExistingId_ReturnsPlanet() {
    Planet savedPlanet = testEntityManager.persistFlushFind(PLANET);
    Optional<Planet> planet = planetRepository.findById(savedPlanet.getId());
    assertThat(planet).isNotEmpty();
    Assertions.assertEquals(planet.get(), PLANET);
  }

  @Test
  public void getPlanet_ByNonExistingId_ThrowsException() {
    assertThat(planetRepository.findById(99L)).isEmpty();
  }

  @Test
  public void getPlanet_ByExistingName_ReturnsPlanet() {
    Planet savedPlanet = testEntityManager.persistFlushFind(PLANET);
    Optional<Planet> foundPlanet = planetRepository.findByName(PLANET.getName());
    assertThat(foundPlanet).isNotEmpty();
    assertThat(savedPlanet).isEqualTo(foundPlanet.get());
  }

  @Test
  public void getPlanet_ByNonExistingName_ThrowsException() {
    Optional<Planet> notFoundPlanet = planetRepository.findByName("any planet");
    assertThat(notFoundPlanet).isEmpty();
  }

  @Test
  @Sql(scripts = "/import_planets.sql")
  public void listPlanets_ReturnsFilteredPlanets() {
    Example<Planet> queryWithoutFilters = QueryBuilder.makeQuery(new Planet());
    Example<Planet> queryWithFilters = QueryBuilder.makeQuery(new Planet(TATOOINE.getClimate(),
        TATOOINE.getTerrain()));

    List<Planet> responseWithoutFilters = planetRepository.findAll(queryWithoutFilters);
    List<Planet> responseWithFilters = planetRepository.findAll(queryWithFilters);

    assertThat(responseWithoutFilters).hasSize(3);
    assertThat(responseWithFilters).hasSize(1);
    assertThat(responseWithFilters.get(0)).isEqualTo(TATOOINE);
  }

  @Test
  public void listPlanets_ReturnsNoPlanets() {
    Example<Planet> queryWithoutFilters = QueryBuilder.makeQuery(new Planet());
    Example<Planet> queryWithFilters = QueryBuilder.makeQuery(new Planet(TATOOINE.getClimate(),
        TATOOINE.getTerrain()));

    List<Planet> responseWithoutFilters = planetRepository.findAll(queryWithoutFilters);
    List<Planet> responseWithFilters = planetRepository.findAll(queryWithFilters);

    assertThat(responseWithoutFilters).hasSize(0);
    assertThat(responseWithFilters).hasSize(0);
  }

  @Test
  public void removePlanet_WithExistingId_RemovesPlanetFromDatabase() {
    Planet planet = testEntityManager.persistFlushFind(PLANET);
    planetRepository.deleteById(planet.getId());
    Planet removedPlanet = testEntityManager.find(Planet.class, planet.getId());
    assertThat(removedPlanet).isNull();
  }


  @Test
  @Sql(scripts = "/import_planets.sql")
  public void removePlanet_WithNonExistingId_DoestNotChangePlanetList() {
    planetRepository.deleteById(4L);
    assertThat(testEntityManager.find(Planet.class, 1L)).isInstanceOf(Planet.class);
    assertThat(testEntityManager.find(Planet.class, 2L)).isInstanceOf(Planet.class);
    assertThat(testEntityManager.find(Planet.class, 3L)).isInstanceOf(Planet.class);
  }
}
