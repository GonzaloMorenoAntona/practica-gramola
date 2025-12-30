package edu.uclm.esi.gramola.model;

import jakarta.persistence.*;

@Entity
@Table(name = "prices") // La tabla en BDD se llamará 'prices'
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;    // Ej: "Monthly Subscription"

    @Column(nullable = false)
    private Double value;   // Ej: 9.99

    @Column(nullable = false)
    private String type;    // Ej: "SUBSCRIPTION" o "SONG"

    // Constructores
    public Price() {}

    public Price(String name, Double value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
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
}