package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.TipoPelicula;

@Entity @Table(name = "pelicula")
public class Pelicula {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPelicula")
    private Long idPelicula;

    @Column(nullable = false) private String titulo;
    @Column(name = "duracionMinutos") private int duracionMinutos;

    @Enumerated(EnumType.STRING) @Column(name = "tipo")
    private TipoPelicula tipo;

    public Pelicula() {}
    public Pelicula(String titulo, int duracionMinutos, TipoPelicula tipo) {
        setTitulo(titulo); setDuracionMinutos(duracionMinutos); setTipo(tipo);
    }

    public void setTitulo(String t){ if (t == null || t.isBlank()) throw new IllegalArgumentException("Título vacío"); this.titulo = t; }
    public void setDuracionMinutos(int d){ if (d <= 0) throw new IllegalArgumentException("Duración > 0"); this.duracionMinutos = d; }
    public void setTipo(TipoPelicula tp){ if (tp == null) throw new IllegalArgumentException("Tipo nulo"); this.tipo = tp; }

    public Long getIdPelicula(){ return idPelicula; }
    public String getTitulo(){ return titulo; }
    public int getDuracionMinutos(){ return duracionMinutos; }
    public TipoPelicula getTipo(){ return tipo; }
    @Override public String toString(){ return titulo + " (" + duracionMinutos + " min, " + tipo + ")"; }
}
