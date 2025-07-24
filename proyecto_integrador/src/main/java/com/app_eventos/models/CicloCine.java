package com.app_eventos.models;

import java.time.LocalDate;
import java.util.List;

public class CicloCine extends Evento {
    private String genero;
    private Persona presentador;
    private List<Pelicula> peliculas;

    // Constructor
    public CicloCine(int codigo, String nombre, String descripcion, LocalDate fechaInicio, LocalDate fechaFin,
                     String genero, Persona presentador, List<Pelicula> peliculas) {
        super(codigo, nombre, descripcion, fechaInicio, fechaFin);
        this.genero = genero;
        this.presentador = presentador;
        this.peliculas = peliculas;
    }

    // Getters y Setters
    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Persona getPresentador() {
        return presentador;
    }

    public void setPresentador(Persona presentador) {
        this.presentador = presentador;
    }

    public List<Pelicula> getPeliculas() {
        return peliculas;
    }

    public void setPeliculas(List<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }
}