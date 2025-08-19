package com.app_eventos.model;

import jakarta.persistence.*;
import com.app_eventos.model.enums.TipoPelicula;

@Entity
@Table(name = "pelicula")
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPelicula")
    private Long idPelicula;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "duracionMinutos")
    private int duracionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoPelicula tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ciclo_id",
        foreignKey = @ForeignKey(name = "fk_pelicula_ciclo"),
        nullable = true
    )
    private CicloCine cicloCine;

    public Pelicula() { }

    public Pelicula(String titulo, int duracionMinutos, TipoPelicula tipo) {
        setTitulo(titulo);
        setDuracionMinutos(duracionMinutos);
        setTipo(tipo);
    }

    // ===== Getters/Setters =====
    public Long getIdPelicula() { return idPelicula; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String t) {
        if (t == null || t.isBlank()) throw new IllegalArgumentException("El Título no puede estar vacío.");
        this.titulo = t;
    }

    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int d) {
        if (d <= 0) throw new IllegalArgumentException("La Duración debe ser mayor a 0.");
        this.duracionMinutos = d;
    }

    public TipoPelicula getTipo() { return tipo; }
    public void setTipo(TipoPelicula tp) {
        if (tp == null) throw new IllegalArgumentException("Tipo nulo");
        this.tipo = tp;
    }

    public CicloCine getCicloCine() { return cicloCine; }
    public void setCicloCine(CicloCine ciclo) { this.cicloCine = ciclo; }

    @Override
    public String toString() {
        return titulo + " (" + duracionMinutos + " min, " + tipo + ")";
    }
}
