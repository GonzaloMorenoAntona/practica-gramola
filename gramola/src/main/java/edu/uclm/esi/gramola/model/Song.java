package edu.uclm.esi.gramola.model; 

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String artist;
    private String date; 
    private String bar;

    public Song() {}

    // Constructor con datos
    public Song(String title, String artist, String date, String bar) {
        this.title = title;
        this.artist = artist;
        this.date = date;
        this.bar = bar;
    }

    // Getters y Setters (puedes generarlos con clic derecho -> Source -> Generate...)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getBar() { return bar; }
    public void setBar(String bar) { this.bar = bar; }
}