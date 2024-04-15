package com.example.swplanetapi.domain;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

public class QueryBuilder {
  public static Example<Planet> makeQuery(Planet planet) {
    ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll().withIgnoreCase().withIgnoreNullValues();
    return Example.of(planet, exampleMatcher);
  }
}