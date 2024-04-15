package com.example.swplanetapi.exception;

public class PlanetNotFoundException extends NotFoundException {
  public PlanetNotFoundException() {
    super("Planet not found");
  }
}
