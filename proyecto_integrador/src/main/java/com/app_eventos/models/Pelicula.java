package com.app_eventos.models;

public class Pelicula {
    private String nombre;
    private String director;
    private int anioEstreno;
    private int duracionMinutos;

    // Constructor
    public Pelicula(String nombre, String director, int anioEstreno, int duracionMinutos) {
        this.nombre = nombre;
        this.director = director;
        this.anioEstreno = anioEstreno;
        this.duracionMinutos = duracionMinutos;
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getAnioEstreno() {
        return anioEstreno;
    }

    public void setAnioEstreno(int anioEstreno) {
        this.anioEstreno = anioEstreno;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    @Override
    public String toString() {
        return nombre + " (" + anioEstreno + ")";
    }
}