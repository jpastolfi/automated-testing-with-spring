package com.example.swplanetapi.domain;
import com.example.swplanetapi.exception.PlanetNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

@Service
public class PlanetService {
  private PlanetRepository planetRepository;
  @Autowired
  public PlanetService(PlanetRepository planetRepository) {
    this.planetRepository = planetRepository;
  }

  public Planet create(Planet planet) {
    return planetRepository.save(planet);
  }

  public Optional<Planet> getById(Long id) throws PlanetNotFoundException {
    return planetRepository.findById(id);
  }

  public Optional<Planet> getByName(String name) {
    return planetRepository.findByName(name);
  }

  public List<Planet> list(String climate, String terrain) {
    Example<Planet> query = QueryBuilder.makeQuery(new Planet(climate, terrain));
    return planetRepository.findAll(query);
  }

  public void deleteById(Long planetId) {
    planetRepository.deleteById(planetId);
  }
}
