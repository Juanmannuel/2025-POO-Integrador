package com.app_eventos.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.app_eventos.model.enums.EstadoEvento;
import com.app_eventos.model.enums.TipoEvento;
import com.app_eventos.model.interfaces.IEventoConInscripcion;

public class CicloCine extends Evento implements IEventoConInscripcion {

    private List<Pelicula> peliculas = new ArrayList<>();
    private boolean postCharla;
    private int inscriptos;

    // Constructor
    public CicloCine(String nombre,
                     LocalDateTime fechaInicio,
                     LocalDateTime fechaFin,
                     boolean postCharla) {
        super(nombre, fechaInicio, fechaFin, TipoEvento.CICLO_CINE);
        this.postCharla = postCharla;
        this.inscriptos = 0;
    }

    public CicloCine() {
        super();
        this.setTipoEvento(TipoEvento.CICLO_CINE);
        this.inscriptos = 0;
    }

    // Modelo RICO
    public void agregarPelicula(Pelicula pelicula) {
        this.peliculas.add(pelicula);
    }

    public void quitarPelicula(Pelicula pelicula) {
        this.peliculas.remove(pelicula);
    }

    @Override
    public void inscribir(Persona participante) {
        if (getEstado() != EstadoEvento.CONFIRMADO) {
            throw new IllegalStateException("El evento debe estar confirmado para inscribir.");
        }
        this.inscriptos++;
    }

    // Getters y Setters
    public List<Pelicula> getPeliculas() {
        return peliculas;
    }

    public void setPeliculas(List<Pelicula> peliculas) {
        this.peliculas = peliculas;
    }

    public boolean isPostCharla() {
        return postCharla;
    }

    public void setPostCharla(boolean postCharla) {
        this.postCharla = postCharla;
    }

    public int getInscriptos() {
        return inscriptos;
    }

    public void setInscriptos(int inscriptos) {
        this.inscriptos = inscriptos;
    }
}
