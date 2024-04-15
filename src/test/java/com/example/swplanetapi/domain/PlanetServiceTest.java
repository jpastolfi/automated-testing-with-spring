package com.example.swplanetapi.domain;

import com.example.swplanetapi.exception.PlanetNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import static com.example.swplanetapi.common.PlanetConstants.INVALID_PLANET;
import static com.example.swplanetapi.common.PlanetConstants.PLANET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanetServiceTest {
  @InjectMocks
  private PlanetService planetService;

  @Mock
  private PlanetRepository planetRepository;

  @Test
  public void cratePlanet_WithValidData_ReturnsPlanet() {
    // Arrange
    when(planetRepository.save(PLANET)).thenReturn(PLANET);

    // sut = system under test
    // Act
    Planet sut = planetService.create(PLANET);

    // Assert
    assertThat(sut).isEqualTo(PLANET);
  }

  @Test
  public void createPlanet_WithInvalidData_ThrowsException() {
    when(planetRepository.save(INVALID_PLANET)).thenThrow(RuntimeException.class);
    assertThatThrownBy(() -> planetService.create(INVALID_PLANET)).isInstanceOf(RuntimeException.class);
  }

  @Test
  public void getPlanet_ByIExistingd_ReturnsPlanet() throws PlanetNotFoundException {
    when(planetRepository.findById(any())).thenReturn(Optional.of(PLANET));
    assertThat(planetService.getById(1L)).isEqualTo(Optional.of(PLANET));
  }

  @Test
  public void getPlanet_ByNonExistingId_ThrowsException() throws PlanetNotFoundException {
    when(planetRepository.findById(any())).thenReturn(Optional.empty());
    assertThat(planetService.getById(any())).isEqualTo(Optional.empty());
  }

  @Test
  public void getPlanet_ByExistingName_ReturnsPlanet() {
    when(planetRepository.findByName(anyString())).thenReturn(Optional.of(PLANET));
    Optional<Planet> sut = planetService.getByName("Biribinha");
    assertThat(sut).isNotEmpty();
    assertThat(sut.get()).isEqualTo(PLANET);
  }

  @Test
  public void getPlanet_ByNonExistingName_ThrowsException() {
    when(planetRepository.findByName(anyString())).thenReturn(Optional.empty());
    Optional<Planet> sut = planetService.getByName("Nadinha");
    assertThat(sut).isEmpty();
    assertThat(sut).isEqualTo(Optional.empty());
  }

  @Test
  public void listPlanets_ReturnsAllPlanets() {
    List<Planet> planets = new ArrayList<>() {{
      add(PLANET);
    }};
    Example<Planet> query = QueryBuilder.makeQuery(new Planet(PLANET.getClimate(), PLANET.getClimate()));

    when(planetRepository.findAll(query)).thenReturn(planets);
    List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getClimate());
    assertThat(sut).isNotEmpty();
    assertThat(sut).hasSize(1);
    assertThat(sut).isInstanceOf(List.class);
  }

  @Test
  public void listPlanets_ReturnsNoPlanets() {
    when(planetRepository.findAll(any())).thenReturn(List.of());
    List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getClimate());
    assertThat(sut).isEmpty();
    assertThat(sut).isInstanceOf(List.class);
  }

  @Test
  public void removePlanet_WithExistingId_DoesNotThrowException() {
    assertThatCode(() -> planetRepository.deleteById(anyLong())).doesNotThrowAnyException();
  }

  @Test
  public void removePlanet_WithNonExistingId_ThrowsException() {
    doThrow(new RuntimeException()).when(planetRepository).deleteById(anyLong());
    assertThatThrownBy(() -> planetService.deleteById(anyLong())).isInstanceOf(RuntimeException.class);
  }


}
