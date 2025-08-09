package com.app_eventos.model;

import com.app_eventos.model.enums.TipoPelicula;

public class Pelicula {

    private Long idPelicula;
    private String titulo;
    private int duracionMinutos;
    private TipoPelicula tipo; // ✅ ahora siempre enum

    // Constructor principal (obligatorio título, duración y tipo)
    public Pelicula(String titulo, int duracionMinutos, TipoPelicula tipo) {
        setTitulo(titulo);
        setDuracionMinutos(duracionMinutos);
        setTipo(tipo);
    }

   // Validaciones

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

    public void setTipo(TipoPelicula tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de película es obligatorio");
        }
        this.tipo = tipo;
    }

    // Getters

    public Long getIdPelicula() { return idPelicula; }
    public String getTitulo() { return titulo; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public TipoPelicula getTipo() { return tipo; }

    @Override
    public String toString() {
        return titulo + " (" + duracionMinutos + " min, " + tipo.getEtiqueta() + ")";
    }
}
