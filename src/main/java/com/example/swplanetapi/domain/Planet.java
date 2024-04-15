package com.example.swplanetapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
@Table(name = "planets")
public class Planet {

  public Planet(String name, String climate, String terrain) {
    this.name = name;
    this.climate = climate;
    this.terrain = terrain;
  }

  public Planet() {
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String climate;
  private String terrain;

  public Planet(String climate, String terrain) {
    this.climate = climate;
    this.terrain = terrain;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getClimate() {
    return climate;
  }

  public void setClimate(String climate) {
    this.climate = climate;
  }

  public String getTerrain() {
    return terrain;
  }

  public void setTerrain(String terrain) {
    this.terrain = terrain;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
}
