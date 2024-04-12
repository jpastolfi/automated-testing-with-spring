package com.example.swplanetapi.domain;

import com.example.swplanetapi.domain.Planet;
import com.example.swplanetapi.domain.PlanetRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
