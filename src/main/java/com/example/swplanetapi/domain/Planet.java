package com.example.swplanetapi.domain;

import com.example.swplanetapi.jacoco.ExcludeFromJacocoGeneratedReport;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
@Table(name = "planets")
public class Planet {

  public Planet(Long id, String name, String climate, String terrain) {
    this.id = id;
    this.name = name;
    this.climate = climate;
    this.terrain = terrain;
  }

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
  @NotEmpty
  @Column(unique = true)
  private String name;
  @NotEmpty
  private String climate;
  @NotEmpty
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

  @Override
  @ExcludeFromJacocoGeneratedReport
  public String toString() {
    return "Planet{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", climate='" + climate + '\'' +
        ", terrain='" + terrain + '\'' +
        '}';
  }
}
