package com.app_eventos.model;

public class Pelicula {

    private Long idPelicula;
    private String titulo;
    private int duracionMinutos;

    // Constructor
    public Pelicula(String titulo, int duracionMinutos) {
        setTitulo(titulo);
        setDuracionMinutos(duracionMinutos);
    }

    public Pelicula() {}

    // Validaciones modelo rico
    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        this.titulo = titulo;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0");
        }
        this.duracionMinutos = duracionMinutos;
    }

    // Getters

    public Long getIdPelicula() {
        return idPelicula;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    @Override
    public String toString() {
        return titulo + " (" + duracionMinutos + " min)";
    }
}
