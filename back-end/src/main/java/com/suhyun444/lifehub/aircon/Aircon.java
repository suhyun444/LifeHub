package com.suhyun444.lifehub.aircon;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Aircon {
    
    @Id
    private Long id;
    private Integer temperature;

    protected Aircon() {}

    public Aircon(Long id, Integer temperature) {
        this.id = id;
        this.temperature = temperature;
    }

    public Long getId() { return id; }
    public Integer getTemperature() { return temperature; }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }
}