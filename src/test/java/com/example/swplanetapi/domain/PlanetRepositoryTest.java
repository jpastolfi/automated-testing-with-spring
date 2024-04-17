package com.example.swplanetapi.domain;

import static com.example.swplanetapi.common.PlanetConstants.EMPTYPLANET;
import static com.example.swplanetapi.common.PlanetConstants.NULLPLANET;
import static com.example.swplanetapi.common.PlanetConstants.PLANET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// Notação para criação de um banco H2 para os testes
@DataJpaTest
public class PlanetRepositoryTest {

  @Autowired
  private PlanetRepository planetRepository;

  // Permite interagir com o banco de dados sem usar a repository
  @Autowired
  private TestEntityManager testEntityManager;

  @Test
  public void createPlanet_WithValidData_ReturnsPlanet() {
    Planet planet = planetRepository.save(PLANET);
    Planet sut = testEntityManager.find(Planet.class, planet.getId());
    assertThat(sut).isNotNull();
    assertThat(sut.getName()).isEqualTo(planet.getName());
    assertThat(sut.getClimate()).isEqualTo(planet.getClimate());
    assertThat(sut.getTerrain()).isEqualTo(planet.getTerrain());
  }

  @Test
  public void createPlanet_WithInvalidData_ThrowsException() {
    assertThatThrownBy(() -> planetRepository.save(EMPTYPLANET)).isInstanceOf(RuntimeException.class);
    assertThatThrownBy(() -> planetRepository.save(NULLPLANET)).isInstanceOf(RuntimeException.class);
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
}
