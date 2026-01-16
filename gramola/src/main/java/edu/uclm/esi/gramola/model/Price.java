package edu.uclm.esi.gramola.model;

import jakarta.persistence.*;

@Entity
@Table(name = "prices") 
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;    // Ej: "Subscripcion mensual"

    @Column(nullable = false)
    private Double value;   // Ej: 9.99

    @Column(nullable = false)
    private String type;    // Ej: "SUBSCRIPTION" o "SONG"

    @Column(nullable = false)
    private Long duration;  // Duración en segundos 

    public Price() {}

    public Price(String name, Double value, String type, Long duration) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.duration = duration;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
}