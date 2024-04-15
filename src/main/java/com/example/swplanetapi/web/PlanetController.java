package com.example.swplanetapi.web;

import com.example.swplanetapi.domain.Planet;
import com.example.swplanetapi.domain.PlanetService;
import com.example.swplanetapi.exception.PlanetNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/planets")
public class PlanetController {
  private PlanetService planetService;

  @Autowired
  public PlanetController(PlanetService planetService) {
    this.planetService = planetService;
  }

  @PostMapping
  public ResponseEntity<Planet> create(@RequestBody Planet planet) {
    Planet createdPlanet = planetService.create(planet);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdPlanet);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Planet> getById(@PathVariable Long id) throws PlanetNotFoundException {
    return planetService.getById(id).map(planet -> ResponseEntity.ok(planet))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<Planet> getByName(@PathVariable String name) {
    return planetService.getByName(name).map(planet -> ResponseEntity.ok(planet))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping()
  public ResponseEntity<List<Planet>> getByClimateAndTerrain(@RequestParam(required = false) String climate, @RequestParam(required = false) String terrain) {
    List<Planet> planets = planetService.list(terrain, climate);
    return ResponseEntity.ok(planets);
  }

  @DeleteMapping("/{planetId}")
  public ResponseEntity<Void> deleteById(@PathVariable Long planetId) {
    planetService.deleteById(planetId);
    return ResponseEntity.noContent().build();
  }
}
