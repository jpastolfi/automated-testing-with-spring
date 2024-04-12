package com.example.swplanetapi.domain;

import com.example.swplanetapi.domain.Planet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanetRepository extends JpaRepository<Planet, Long> {

}
